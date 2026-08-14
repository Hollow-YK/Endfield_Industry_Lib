package endfieldindustrylib.EFworld.ai;

import arc.graphics.Color;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.util.Time;
import static mindustry.Vars.headless;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;
import endfieldindustrylib.EFworld.unit.Landbreakers.Vanguard;
import mindustry.ai.types.GroundAI;
import mindustry.content.Fx;
import mindustry.core.World;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

/**
 * 伏击者（Ambusher）专属 AI —— 人形弩手，"极其智慧"的战术型 AI。
 * <p>
 * 每帧按优先级评估行为状态机：
 * <ol>
 *   <li><b>被近身 → 逃离</b>：敌人进入 {@link #fleeRange} 贴身距离时，立即向远离敌人的方向
 *       高速撤退（走标准移动链路 {@code movePref}，身体转向逃离方向、走路动画正常），
 *       撞墙/卡住自动重掷方向；边退边面向敌人，弩可独立瞄准还击。</li>
 *   <li><b>换弹 → 寻找掩体</b>：武器仍在装填（{@code mount.reload > 0}，无法开火）时，
 *       向敌人背后的扇形方向扫描"可站立且敌人视线被墙遮挡"的点作为掩体并前往躲藏，
 *       换弹期间不暴露；找不到掩体则退到武器射程内缘的战术位，减少暴露。</li>
 *   <li><b>正常交战</b>：射程外推进、射程内原地射击（武器 autoTarget 自动索敌）；保持安全距离。</li>
 *   <li><b>无目标</b>：交由 {@link GroundAI} 标准逻辑朝核心寻路。</li>
 * </ol>
 * 索敌与 StingAI 相同（单位 > 炮台 > 建筑 > 墙体），可对空/对地。
 */
public class AmbusherAI extends GroundAI{
    /** 索敌半径（世界单位）：20 格 */
    public static final float detectRange = 20f * tilesize;
    /** 武器射程（世界单位，与弩箭弹道匹配，略留余量）：18 格 */
    public static final float weaponRange = 18f * tilesize;
    /** 被近身判定距离（世界单位）：敌人进入此距离立刻逃离 */
    public static final float fleeRange = 8f * tilesize;
    /** 掩体搜索最大半径（世界单位） */
    public static final float coverSearchRange = 12f * tilesize;
    /** 走位平滑步长（数值越大越早减速） */
    private static final float attackSmooth = 40f;
    /** 逃离速度倍率（相对普通移速） */
    private static final float fleeSpeedMul = 1.35f;
    /** 逃离方向重掷间隔（帧）：撞墙/卡住也立即重掷 */
    private static final float fleeRetargetInterval = 30f;
    /** 掩体重查间隔（帧）：敌人移动后掩体可能失效 */
    private static final float coverRecheckInterval = 25f;
    /** 站立扬尘计时（帧） */
    private float dustTimer = 0f;

    /** 复用向量 */
    private final Vec2 tmp = new Vec2();
    /** 当前掩体目标点 */
    private final Vec2 coverPoint = new Vec2();
    /** 是否持有有效掩体点 */
    private boolean hasCover = false;
    /** 掩体重查计时（帧） */
    private float coverRecheck = 0f;
    /** 逃离方向（度） */
    private float fleeDir = 0f;
    /** 逃离方向重掷计时（帧） */
    private float fleeRetarget = 0f;

    @Override
    public void updateMovement(){
        // —— 索敌：按仇恨优先级取目标，否则朝核心寻路（标准 GroundAI） ——
        Position nextTarget = findTarget();

        if(nextTarget == null){
            // 无目标：清除掩体状态，按 GroundAI 标准逻辑朝核心寻路
            hasCover = false;
            super.updateMovement();
            kickDust();
            return;
        }

        float dst = unit.dst(nextTarget);

        // 1. 被近身 → 逃离（最高优先级）
        if(dst < fleeRange){
            flee(nextTarget);
        }else if(isReloading()){
            // 2. 换弹中（无法开火）→ 寻找掩体躲避
            seekCover(nextTarget);
        }else{
            // 3. 可开火 → 正常远程交战
            attackMove(nextTarget);
        }

        kickDust();
    }

    /** 任一可控武器正处于换弹/冷却（reload > 0，无法开火） */
    private boolean isReloading(){
        for(var mount : unit.mounts){
            if(mount.weapon.controllable && mount.reload > 0f){
                return true;
            }
        }
        return false;
    }

    /** 逃离：向远离敌人的方向高速撤退；撞墙/卡住自动重掷方向，边退边面向敌人 */
    private void flee(Position threat){
        hasCover = false;

        // 撞墙/卡住或定时 → 重掷逃离方向（远离敌人 + 随机偏转，避免跑进死角）
        if(unit.onSolid() || (fleeRetarget -= Time.delta) <= 0f){
            fleeRetarget = fleeRetargetInterval;
            fleeDir = unit.angleTo(threat) + 180f + Mathf.range(40f);
        }

        // 走标准移动链路（movePref → rotateMove）：身体转向逃离方向、走路动画正常
        tmp.trns(fleeDir, unit.speed() * fleeSpeedMul);
        unit.movePref(tmp);
        // 面向威胁：弩（rotate=true）独立瞄准，可边退边还击
        unit.lookAt(threat);
    }

    /** 换弹战术：前往能遮挡敌人视线的掩体躲藏；找不到掩体则退到武器射程内缘 */
    private void seekCover(Position aim){
        // 已在掩体点上：停下等待换弹完成，面朝敌人方向
        if(hasCover && unit.within(coverPoint.x, coverPoint.y, 2f)){
            unit.lookAt(aim);
            return;
        }

        // 周期性重查掩体点（敌人移动后旧掩体可能失效）
        if((coverRecheck -= Time.delta) <= 0f){
            coverRecheck = coverRecheckInterval;
            hasCover = findCoverPoint(aim, coverPoint);
        }

        if(hasCover){
            moveTo(coverPoint, 2f, 20f);
            // 接近掩体时面朝敌人，准备换弹完成后探出射击
            if(unit.within(coverPoint.x, coverPoint.y, 4f)){
                unit.lookAt(aim);
            }
        }else{
            // 开阔地找不到掩体：退到武器射程内缘附近，减少换弹期间的暴露
            moveTo(aim, weaponRange * 0.85f, 20f, true, null);
        }
    }

    /** 扫描敌人背后的扇形区域，寻找一个可站立且敌人视线被实心格遮挡的点作为掩体 */
    private boolean findCoverPoint(Position aim, Vec2 out){
        float ang = unit.angleTo(aim);
        // 探测方向：远离敌人为主，左右侧为辅（相对"到敌人方向"的角度）
        float[] dirs = {180f, 150f, 210f, 120f, 240f, 90f, 270f};
        for(float rel : dirs){
            float dir = ang + rel;
            for(float d = 2f * tilesize; d <= coverSearchRange; d += tilesize){
                float cx = unit.x + Angles.trnsx(dir, d);
                float cy = unit.y + Angles.trnsy(dir, d);

                Tile t = world.tileWorld(cx, cy);
                if(t == null || t.solid() || !unit.canPass(t.x, t.y)) continue;
                // 掩体点须在安全距离外（避免掩体离敌人太近，敌人绕过来就被抓）
                if(aim.dst(cx, cy) < fleeRange) continue;
                // 敌人与候选点之间视线被挡住（地形或友军持盾先锋），则视为有效掩体
                if(blockedTo(cx, cy, aim)){
                    out.set(cx, cy);
                    return true;
                }
            }
        }
        return false;
    }

    /** 正常远程交战：射程外推进、射程内原地射击（武器 autoTarget 自动索敌） */
    private void attackMove(Position aim){
        if(unit.dst(aim) > weaponRange){
            // 射程外：向目标推进，进入射程即停
            moveTo(aim, weaponRange, attackSmooth);
        }
        // 射程内：保持原地，武器自动索敌射击
        unit.lookAt(aim);
    }

    /** 两点之间视线是否被实心格挡住（目标所在格不算障碍，避免贴脸误判） */
    private boolean lineBlocked(float x, float y, Position target){
        int tx = World.toTile(target.getX()), ty = World.toTile(target.getY());
        return World.raycast(World.toTile(x), World.toTile(y), tx, ty, (px, py) -> {
            Tile tile = world.tile(px, py);
            if(tile != null && tile.build == target) return false;
            return tile == null || tile.solid();
        });
    }

    /** 候选点与敌人之间是否被遮挡：地形（实心格）或友军持盾先锋（Vanguard），后者可作为掩体 */
    private boolean blockedTo(float x, float y, Position target){
        return lineBlocked(x, y, target) || allyShieldBlocks(x, y, target);
    }

    /** 沿候选点指向敌人的射线采样：附近是否有友军持盾先锋，有则其盾牌可作为掩体 */
    private boolean allyShieldBlocks(float x, float y, Position target){
        float dx = target.getX() - x, dy = target.getY() - y;
        int steps = Math.max(1, Mathf.ceil(Mathf.len(dx, dy) / tilesize));
        for(int i = 0; i <= steps; i++){
            float px = x + dx * i / steps, py = y + dy * i / steps;
            boolean[] found = {false};
            Units.nearby(unit.team, px, py, 9f, u -> {
                if(!found[0] && u instanceof Vanguard v && v.hasShield()){
                    found[0] = true;
                }
            });
            if(found[0]) return true;
        }
        return false;
    }

    /** 扬起沙尘（约每 0.12 秒一团） */
    private void kickDust(){
        if((dustTimer += Time.delta) >= 7f){
            dustTimer = 0f;
            if(!headless){
                Floor floor = unit.floorOn();
                float dustScale = unit.type.hitSize / 8f;
                if(floor != null){
                    floor.walkEffect.at(unit.x, unit.y, dustScale, floor.mapColor);
                }else{
                    Fx.unitLandSmall.at(unit.x, unit.y, dustScale, Color.white);
                }
            }
        }
    }

    /** 建筑目标候选聚合器：在 lambda 内累计炮台/建筑/墙体中最高类别且最近的目标 */
    private static class BuildingTarget{
        Building best;
        int bestCat = -1;
        float bestDist;
    }

    /**
     * 仇恨优先级索敌：单位 > 炮台 > 建筑 > 墙体。
     * 同类别内取最近目标，更高类别无视距离优先；普通建筑排除低矮（传送带类）方块。
     */
    private Position findTarget(){
        // 1. 单位优先（含空中，与弩箭 collidesAir 一致）
        Unit enemy = Units.closestEnemy(unit.team, unit.x, unit.y, detectRange, u -> u.checkTarget(true, true));
        if(enemy != null) return enemy;

        // 2-4. 炮台 > 建筑 > 墙体：一次遍历，同类取最近、高类别无视距离优先
        BuildingTarget t = new BuildingTarget();
        Units.nearbyBuildings(unit.x, unit.y, detectRange, b -> {
            if(b.team == unit.team || !b.block.targetable || !b.isValid() || !b.isDiscovered(unit.team)) return;

            // 类别：炮台(2) > 普通建筑(1) > 墙体(0)
            int cat = b.block.attacks ? 2 : (b.block.solid ? 0 : 1);
            // 普通建筑：排除低矮（传送带类）方块；炮台/墙体始终可索敌
            if(cat == 1 && isLowBuilding(b)) return;

            float dist = b.dst(unit.x, unit.y);
            if(cat > t.bestCat || (cat == t.bestCat && (t.best == null || dist < t.bestDist))){
                t.bestCat = cat;
                t.bestDist = dist;
                t.best = b;
            }
        });
        return t.best;
    }

    /** 是否低矮建筑（传送带类）：单格、非实心的小型物流/节点方块，不值得攻击 */
    private boolean isLowBuilding(Building b){
        return !b.block.solid && b.block.size == 1;
    }
}
