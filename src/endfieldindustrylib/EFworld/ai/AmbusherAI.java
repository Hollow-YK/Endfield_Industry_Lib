package endfieldindustrylib.EFworld.ai;

import arc.graphics.Color;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.struct.IntIntMap;
import arc.struct.IntSeq;
import arc.util.Log;
import arc.util.Time;
import endfieldindustrylib.EFworld.unit.Landbreakers.Shield;
import endfieldindustrylib.EFworld.unit.Landbreakers.Vanguard;
import static mindustry.Vars.headless;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;
import mindustry.ai.types.GroundAI;
import mindustry.content.Fx;
import mindustry.core.World;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

/**
 * 伏击者（Ambusher）专属 AI —— 人形弩手，"极其智慧"的战术型 AI。
 * <p>
 * 每帧按优先级评估行为状态机：
 * <ol>
 *   <li><b>被近身 → 逃离</b>：敌人进入 {@link #fleeRange} 贴身距离时，<b>锁定该目标</b>并向远离方向
 *       带绕墙撤退（逃跑期间不切换目标，避免被两个敌人夹击时在 A/B 间左右横跳）；
 *       撤退有界（拉到脱离距离即止，不再越退越远）；带滞回避免在边界反复抽搐。</li>
 *   <li><b>换弹 → 寻找掩体</b>：武器仍在装填（{@code mount.reload > 0}，无法开火）时，
 *       优先复用上一次掩体，否则向敌人背后的扇形方向扫描"可站立且敌人视线被墙遮挡"的点
 *       作为掩体（取离自身最近的有效点，3 秒换弹内能快速躲进去）并前往躲藏，途中遇敌弹闪避；
 *       找不到掩体则小幅走位等待。</li>
 *   <li><b>开火 → 探出掩体</b>：装填完成后先走出掩体（到敌人可见处）再射击；看不到敌人不空射。</li>
 *   <li><b>正常交战</b>：无视线时绕墙找可见站位（绝不贴脸冲锋）、射程外推进到射程边缘、
 *       射程内横移走位（保持交战距离 + 主动闪避敌弹）射击；近身交由逃离处理。</li>
 *   <li><b>无目标</b>：交由 {@link GroundAI} 标准逻辑朝核心寻路。</li>
 * </ol>
 * 索敌与 StingAI 相同（单位 > 炮台 > 建筑 > 墙体），可对空/对地；
 * 单位内优先打"当前射程内且视线通"的目标，敌方盾牌（吸收弩箭）只在无其它目标时兜底。
 * <p>
 * 移动采用<b>计划制</b>：每个行为设定一个"计划目标"（{@link #planTarget}），
 * BFS 寻路后<b>逐格路点跟随</b>（平滑、不卡角、不乱走），被墙挡时沿"更接近目标"的一侧滑墙；
 * 掩体/侧翼/探出点定时重查、风筝走位锚点提交，避免每帧换向的"乱走"。
 * 调试：{@link #debug}=true 时在地图上画状态/目标/寻路路径悬浮文本，并把每步计划详情打印到终端。
 */
public class AmbusherAI extends GroundAI{
    /** 索敌半径（世界单位）：26 格（比武射程 18 格留 8 格余量，避免目标贴着索敌边界导致 IDLE↔交战抽搐） */
    public static final float detectRange = 26f * tilesize;
    /** 脱离锁定距离（世界单位）：原索敌半径的 1.5 倍。找掩体走出索敌范围时仍保持锁定，避免反复抽搐 */
    public static final float deTargetRange = detectRange * 1.5f;
    /** 武器射程/交战距离（世界单位）：16 格。与弩箭弹道（约 16.9 格）匹配；开火严格限制在此范围内 */
    public static final float weaponRange = 16f * tilesize;
    /** 被近身判定距离（世界单位）：敌人进入此距离立刻逃离 */
    public static final float fleeRange = 8f * tilesize;
    /** 掩体搜索最大半径（世界单位）：限制在近处，避免跑出索敌范围 */
    public static final float coverSearchRange = 8f * tilesize;
    /** 逃离速度倍率（相对普通移速） */
    private static final float fleeSpeedMul = 1.35f;
    /** 掩体重查间隔（帧）：敌人移动后掩体可能失效 */
    private static final float coverRecheckInterval = 25f;
    /** 站立扬尘计时（帧） */
    private float dustTimer = 0f;

    /** 复用向量 */
    private final Vec2 tmp = new Vec2();
    /** 当前掩体目标点 */
    private final Vec2 coverPoint = new Vec2();
    /** 探出掩体后的射击站位 */
    private final Vec2 peekPoint = new Vec2();
    /** 上一次使用的掩体点（射击换弹后优先复用，避免反复重新找掩体） */
    private final Vec2 lastCoverPoint = new Vec2();
    /** 当前锁定目标（超出脱离距离才丢弃，防止找掩体走出索敌范围后反复抽搐） */
    private Position lockedTarget = null;
    /** 是否持有有效掩体点 */
    private boolean hasCover = false;
    /** 是否已有可复用的上一次掩体点 */
    private boolean hasLastCover = false;
    /** 掩体重查计时（帧） */
    private float coverRecheck = 0f;
    /** 逃离滞回状态：进入逃离后须拉开到脱离距离才解除，避免在边界反复抽搐 */
    private boolean fleeing = false;
    /** 逃离锁定的目标：进入逃离即锁定，避免夹击时 findTarget 在多个敌人间切换导致左右横跳 */
    private Position fleeTarget = null;
    /** 逃离撤退点（世界坐标）：定时刷新而非每帧重算，避免被墙夹击时目标格来回跳→抽搐 */
    private final Vec2 fleePoint = new Vec2();
    private float fleeTimer = 0f;
    private static final float fleeRefreshInterval = 15f;
    /** 寻路 BFS 最大深度（格） */
    private static final int pathLimit = 30;
    /** 寻路 BFS 队列/映射（复用，避免每帧分配） */
    private final IntSeq bfsQueue = new IntSeq();
    private final IntIntMap bfsDist = new IntIntMap();
    private final IntIntMap bfsParent = new IntIntMap();
    /** 锁定目标持续不可见计时（帧）：10 秒超时才放弃锁定（防止换弹躲掩体时因自身挡住视线而 90 帧掉锁→反复 IDLE） */
    private static final float lostLineTimeout = 600f;
    private float lostLineTimer = 0f;
    /** 子弹闪避：探测半径 4 格、来袭夹角 60°、冷却 30 帧、单次闪避持续 12 帧、扫描间隔 8 帧（降开销） */
    private static final float dodgeRadius = 4f * tilesize;
    private static final float dodgeAngle = 60f;
    private static final float dodgeCooldown = 30f;
    private static final float dodgeMoveTime = 12f;
    private static final float dodgeScanInterval = 8f;
    private float dodgeCd = 0f;
    private float dodgeMove = 0f;
    private float dodgeScan = 0f;
    private float dodgeDir = 0f;
    /** 最近威胁子弹位置（复用） */
    private final Vec2 threatPos = new Vec2();

    // —— 计划系统：每个行为设定一个"计划目标"，导航向它推进，避免每帧乱换方向（"乱走"根源） ——
    /** 当前行为计划标签（供调试显示与终端日志） */
    private String plan = "IDLE";
    /** 计划目标点（世界坐标，供调试/导航） */
    private final Vec2 planTarget = new Vec2();
    /** 计划停止距离（世界单位） */
    private float planStopDist = 1.5f;
    /** 探出点/侧翼点是否有效（各自定时重查，不每帧重算） */
    private boolean hasPeekPoint = false;
    private boolean hasFlankPoint = false;
    /** 探出点/侧翼点重查计时（帧） */
    private float peekTimer = 0f;
    private float flankTimer = 0f;
    private static final float peekPlanInterval = 22f;
    private static final float flankPlanInterval = 45f;
    /** 侧翼点离自身最大距离（世界单位）：超过则放弃侧翼改直接推进，避免满地图乱跑 */
    private static final float flankMaxDist = 6f * tilesize;

    // —— 导航：BFS 寻路 + 逐格路点跟随（平滑、不卡角、不乱走） ——
    /** 寻路重算间隔（帧）：仅在换格/超时/目标变化时重算，避免每帧翻路径 */
    private static final float navInterval = 20f;
    private float navTimer = 0f;
    /** 计划时的目标格（用于重算） */
    private int navGX = -1, navGY = -1;
    /** 当前正跟随的路点索引（指向 bfsPath，从末尾向前） */
    private int wpIndex = 0;
    /** 已算出的完整路径（goal→start 顺序存储，从末尾向前跟随） */
    private final IntSeq bfsPath = new IntSeq();
    /** 是否已有有效路径 */
    private boolean hasPath = false;

    // —— 风筝走位（射程内机动）：锚点提交，避免每帧换向 ——
    private final Vec2 kitePoint = new Vec2();
    private boolean hasKite = false;
    private float kiteTimer = 0f;

    // —— 调试：地图悬浮文本 + 终端计划日志 ——
    /** 调试开关：true 时在地图上绘制状态/目标/寻路路径的悬浮文本，并把每步计划详情打印到终端 */
    public static boolean debug = false;
    /** 地图文本刷新间隔（帧） */
    private static final float debugInterval = 5f;
    private float debugTimer = 0f;
    /** 终端周期日志间隔（帧） */
    private static final float logInterval = 30f;
    private float logTimer = 0f;
    /** 上次打印的计划标签（变化时才打印详情） */
    private String lastLogPlan = "";

    @Override
    public void updateMovement(){
        // —— 逃离中：锁定触发逃离的目标，不再切换（避免被两个敌人夹击时在 A/B 间左右横跳） ——
        Position nextTarget;
        if(fleeing && fleeTarget != null && targetValid(fleeTarget)){
            nextTarget = fleeTarget;
        }else{
            if(fleeing){
                // 锁定的逃跑目标失效 → 结束逃离
                fleeing = false;
                fleeTarget = null;
            }
            nextTarget = currentTarget();
        }

        if(nextTarget == null){
            // 无目标：清除掩体与锁定状态，按 GroundAI 标准逻辑朝核心寻路
            hasCover = false;
            lockedTarget = null;
            fleeing = false;
            fleeTarget = null;
            hasLastCover = false;
            plan = "IDLE";
            super.updateMovement();
            kickDust();
            debugStep();
            return;
        }

        float dst = unit.dst(nextTarget);

        // 1. 被近身 → 逃离（最高优先级；滞回避免边界反复抽搐；对任何目标包括静态墙都触发）
        if(fleeing || dst < fleeRange){
            // 首次进入逃离：锁定当前目标，之后不再切换（杜绝左右横跳）
            if(!fleeing){
                fleeing = true;
                fleeTarget = nextTarget;
                lockedTarget = nextTarget;
            }
            flee(nextTarget);
        }else if(isReloading()){
            // 2. 换弹中（无法开火）→ 寻找/复用掩体躲避
            seekCover(nextTarget);
        }else{
            // 3. 可开火 → 先探出掩体再射击
            peekAndShoot(nextTarget);
        }

        kickDust();
        debugStep();
    }

    /** 当前有效目标：优先实时索敌；若找掩体走出索敌范围，则沿用脱离距离内的上次锁定目标，避免反复抽搐。
     *  但锁定目标长期躲在视野外（隔着墙/远距离死磕）时会强制放弃，避免贴脸冲锋或追着一个看不见的目标不放 */
    private Position currentTarget(){
        Position found = findTarget();
        if(found != null){
            lockedTarget = found;
            lostLineTimer = 0f;
            return found;
        }
        if(lockedTarget != null && targetValid(lockedTarget) && unit.within(lockedTarget, deTargetRange)){
            // 目标长期不可见 → 放弃锁定（重新索敌交给 findTarget；无更近目标则回到核心寻路）
            if(lineBlocked(unit.x, unit.y, lockedTarget)){
                lostLineTimer += Time.delta;
                if(lostLineTimer > lostLineTimeout){
                    lockedTarget = null;
                }
            }else{
                lostLineTimer = 0f;
            }
            return lockedTarget;
        }
        lockedTarget = null;
        return null;
    }

    /** 目标是否仍存活/有效 */
    private boolean targetValid(Position t){
        if(t instanceof Unit u) return u.isValid() && !u.dead;
        if(t instanceof Building b) return b.isValid();
        return false;
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

    /** 逃离：带绕墙向后撤退（有界，拉到脱离距离即止，不再越退越远）；滞回避免边界抽搐。
     *  撤退点定时刷新（不每帧重算），避免被墙夹击时目标格来回跳导致抽搐 */
    private void flee(Position threat){
        float dst = unit.dst(threat);
        if(dst > fleeRange * 1.5f){
            fleeing = false;
            fleeTarget = null;   // 已拉开足够距离 → 解除逃离并释放目标锁定，交给上层选掩体/交战
            return;
        }
        fleeing = true;
        hasCover = false;
        // 后退到敌人背后 fleeRange*1.5+2 格处（以敌人为基准的有界撤退，带绕墙寻路）
        if((fleeTimer -= Time.delta) <= 0f){
            fleeTimer = fleeRefreshInterval;
            float back = unit.angleTo(threat) + 180f;
            fleePoint.set(
                threat.getX() + Angles.trnsx(back, fleeRange * 1.5f + 2f),
                threat.getY() + Angles.trnsy(back, fleeRange * 1.5f + 2f));
        }
        plan = "FLEE";
        movePathfind(fleePoint.x, fleePoint.y, tilesize, unit.speed() * fleeSpeedMul);
        // 面向威胁：弩（rotate=false）靠身体朝向瞄准，可边退边还击
        unit.lookAt(threat);
    }

    /** 换弹战术：优先复用上一次掩体，否则寻找能遮挡敌人视线的掩体躲藏；找不到则走位等待 */
    private void seekCover(Position aim){
        // 周期性确认/重查掩体（避免每帧做昂贵的视线采样）
        if((coverRecheck -= Time.delta) <= 0f){
            coverRecheck = coverRecheckInterval;

            if(hasCover){
                // 当前掩体仍有效 → 保留；失效 → 清掉重新找
                hasCover = validCover(coverPoint.x, coverPoint.y, aim);
            }
            if(!hasCover){
                // 优先复用上一次的掩体点（若仍能挡住敌人视线），否则重新搜索
                if(hasLastCover && validCover(lastCoverPoint.x, lastCoverPoint.y, aim)){
                    coverPoint.set(lastCoverPoint);
                    hasCover = true;
                }else{
                    hasCover = findCoverPoint(aim, coverPoint);
                    if(hasCover){
                        lastCoverPoint.set(coverPoint);
                        hasLastCover = true;
                    }
                }
            }
        }

        if(hasCover){
            // 接近掩体（约 2 格，用格而非世界单位）即视为已躲好 → 停下静默装填，不再苛求踩到格心（消除绕掩体转圈）
            if(unit.within(coverPoint.x, coverPoint.y, 2f * tilesize)){
                plan = "HIDE";
                unit.lookAt(aim);
            }else if(tryDodge(unit.angleTo(coverPoint))){
                plan = "COVER";
                planTarget.set(coverPoint);
                unit.lookAt(aim);
            }else{
                plan = "COVER";
                planTarget.set(coverPoint);
                movePathfind(coverPoint.x, coverPoint.y, 1.5f * tilesize, unit.speed());
                if(unit.within(coverPoint.x, coverPoint.y, 4f * tilesize)){
                    unit.lookAt(aim);
                }
            }
        }else{
            // 找不到掩体：小幅走位等待换弹（不再后退，也不站桩挨打）
            plan = "WAIT";
            planTarget.set(aim.getX(), aim.getY());
            strafe(aim);
            unit.lookAt(aim);
        }
    }

    /** 扫描敌人背后的扇形区域，寻找一个可站立且敌人视线被实心格遮挡的点作为掩体（取离自身最近的有效点） */
    private boolean findCoverPoint(Position aim, Vec2 out){
        float ang = unit.angleTo(aim);
        // 探测方向：远离敌人为主，左右侧为辅（相对"到敌人方向"的角度）
        float[] dirs = {180f, 150f, 210f, 120f, 240f, 90f, 270f};
        boolean found = false;
        float bestX = 0f, bestY = 0f, bestDist = Float.MAX_VALUE, bestEnemyDist = Float.MAX_VALUE;
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
                    // 取"离自身最近"的有效掩体：3 秒换弹内能快速躲进去、减少暴露在外的路程；
                    // 距离相同时再优先贴近敌人（便于探出射击）
                    float dist = unit.dst(cx, cy);
                    float enemyDist = aim.dst(cx, cy);
                    if(dist < bestDist || (dist == bestDist && enemyDist < bestEnemyDist)){
                        bestDist = dist;
                        bestEnemyDist = enemyDist;
                        bestX = cx;
                        bestY = cy;
                        found = true;
                    }
                }
            }
        }
        if(found){
            out.set(bestX, bestY);
        }
        return found;
    }

    /** 掩体点是否仍有效：可站立、离敌人够远、且敌人视线被遮挡 */
    private boolean validCover(float x, float y, Position aim){
        Tile t = world.tileWorld(x, y);
        if(t == null || t.solid() || !unit.canPass(t.x, t.y)) return false;
        if(aim.dst(x, y) < fleeRange) return false;
        return blockedTo(x, y, aim);
    }

    /** 可开火：若躲在掩体后则先探出掩体（走到敌人可见处）再射击 */
    private void peekAndShoot(Position aim){
        if(hasCover){
            // 敌人可见 → 已探出掩体，直接射击
            if(!lineBlocked(unit.x, unit.y, aim)){
                hasCover = false;
                attackMove(aim);
                return;
            }
            // 仍在掩体后 → 探出到可见站位再射击
            peekOut(aim);
            return;
        }
        attackMove(aim);
    }

    /** 探出掩体：在附近找一个敌人可见的站位走过去，到达后停下射击（探出点定时重查，不每帧换目标） */
    private void peekOut(Position aim){
        if((peekTimer -= Time.delta) <= 0f){
            peekTimer = peekPlanInterval;
            hasPeekPoint = findPeekPoint(aim, peekPoint);
        }
        if(hasPeekPoint){
            plan = "PEEK";
            planTarget.set(peekPoint);
            planStopDist = 1f * tilesize;
            if(unit.within(peekPoint.x, peekPoint.y, 1f * tilesize)){
                hasCover = false;
                unit.lookAt(aim);
            }else{
                movePathfind(peekPoint.x, peekPoint.y, 1f * tilesize, unit.speed());
                unit.lookAt(aim);
            }
        }else{
            // 附近找不到可见站位 → 绕到敌人交战距离边缘找可见点（不贴脸冲锋）
            approachVisible(aim);
            unit.lookAt(aim);
        }
    }

    /** 绕墙/无视线推进：在敌人四周射程半径附近找一个"能看见敌人、可站立"的点作为绕行目标（取离自身最近），
     *  侧翼点定时重查避免每帧换目标；找不到则推进到射程边缘走位等待，绝不贴脸冲锋 */
    private void approachVisible(Position aim){
        if((flankTimer -= Time.delta) <= 0f){
            flankTimer = flankPlanInterval;
            hasFlankPoint = findFlankPoint(aim, peekPoint);
        }
        if(hasFlankPoint){
            plan = "FLANK";
            planTarget.set(peekPoint);
            planStopDist = 1f * tilesize;
            movePathfind(peekPoint.x, peekPoint.y, 1f * tilesize, unit.speed());
            unit.lookAt(aim);
        }else{
            // 找不到近处可见站位：持续朝目标推进直到获得视线（一旦能看到，下一帧 attackMove 即转入射击）。
            // 停止距离按目标类型取：单位 → 停在 fleeRange 外（不贴脸）；建筑/墙 → 贴近 2 格（保证能绕过障碍看到墙口/建筑）。
            // 原来用固定 weaponRange 会在射程边缘既看不到又打不出而原地发呆。
            plan = "APPROACH";
            planTarget.set(aim.getX(), aim.getY());
            float stop = aim instanceof Unit ? fleeRange : 2f * tilesize;
            planStopDist = stop;
            movePathfind(aim.getX(), aim.getY(), stop, unit.speed());
            unit.lookAt(aim);
        }
    }

    /** 在敌人四周、距离约 0.7~1.15 倍射程处采样，寻找"能看见敌人且可站立"的点，取离自身最近者 */
    private boolean findFlankPoint(Position aim, Vec2 out){
        float ang = unit.angleTo(aim);
        float[] rels = {0f, 45f, -45f, 90f, -90f, 135f, -135f, 180f, 22.5f, -22.5f, 67.5f, -67.5f};
        float[] mults = {1f, 0.85f, 0.7f, 1.15f};
        float best = Float.MAX_VALUE;
        boolean found = false;
        for(float rel : rels){
            for(float m : mults){
                float d = weaponRange * m;
                float px = aim.getX() + Angles.trnsx(ang + rel, d);
                float py = aim.getY() + Angles.trnsy(ang + rel, d);
                Tile t = world.tileWorld(px, py);
                if(t == null || t.solid() || !unit.canPass(t.x, t.y)) continue;
                if(lineBlocked(px, py, aim)) continue;   // 该点必须能看见敌人
                float dist = unit.dst(px, py);
                if(dist > flankMaxDist) continue;   // 侧翼点必须在近处（否则满地图乱跑）
                if(dist < best){
                    best = dist;
                    out.set(px, py);
                    found = true;
                }
            }
        }
        return found;
    }

    /** 带寻路移动：向 (tx,ty) 推进到 stopDist 内停下。
     *  BFS 寻路 → 逐格路点跟随（平滑、不卡角）；被墙挡时沿"更接近目标"的一侧滑墙，避免乱走与顶墙抽搐 */
    private void movePathfind(float tx, float ty, float stopDist, float speed){
        // 记录计划目标（供调试/提交）
        planTarget.set(tx, ty);
        planStopDist = stopDist;

        // 到达目标附近 → 停
        if(unit.within(tx, ty, stopDist)){
            hasPath = false;
            return;
        }

        int gx = World.toTile(tx), gy = World.toTile(ty);

        // 仅在 无路径/定时/目标格变化/当前路点被封/偏离路点过远 时重算（不再因自身换格而每帧翻路径 → 消除乱走）
        boolean wpBlocked = hasPath && wpIndex >= 0 && !passable(bfsPath.get(wpIndex) >> 16, bfsPath.get(wpIndex) & 0xffff);
        boolean offPath = hasPath && wpIndex >= 0 && !unit.within(wpX(wpIndex), wpY(wpIndex), 1.6f * tilesize);
        if(!hasPath || navTimer <= 0f || gx != navGX || gy != navGY || wpBlocked || offPath){
            navTimer = navInterval;
            navGX = gx; navGY = gy;
            hasPath = bfsPath(gx, gy);
        }
        navTimer -= Time.delta;

        if(hasPath){
            // 逐格推进：贴近当前路点（约 0.6 格，用格而非世界单位）→ 前进到上一个（bfsPath 为 goal→start，从末尾向前）
            while(wpIndex >= 0 && unit.within(wpX(wpIndex), wpY(wpIndex), tilesize * 0.6f)){
                wpIndex--;
            }
            if(wpIndex >= 0){
                // 路点被墙封（墙突然出现）→ 重算
                if(!passable(bfsPath.get(wpIndex) >> 16, bfsPath.get(wpIndex) & 0xffff)){
                    hasPath = false;
                    return;
                }
                float dir = slideDir(unit.angleTo(wpX(wpIndex), wpY(wpIndex)), unit.angleTo(tx, ty));
                if(dir < 0f) return;   // 完全无路：原地
                tmp.trns(dir, speed);
                unit.movePref(tmp);
                return;
            }
            hasPath = false;   // 路点走完 → 直接朝目标
        }

        // 无路径/路点走完：直接朝目标推进，遇墙沿开阔且朝目标的一侧滑动（不随机全向乱窜）
        float dir = slideDir(unit.angleTo(tx, ty), unit.angleTo(tx, ty));
        if(dir < 0f) return;
        tmp.trns(dir, speed);
        unit.movePref(tmp);
    }

    /** 路点 x/y（世界坐标，格中心） */
    private float wpX(int i){
        return (bfsPath.get(i) >> 16) * tilesize + tilesize / 2f;
    }

    private float wpY(int i){
        return (bfsPath.get(i) & 0xffff) * tilesize + tilesize / 2f;
    }

    /** 简易 BFS 寻路：从单位所在格到 (gx,gy)，把整条路径存入 bfsPath（goal→start 顺序），
     *  跟随方向 = 从末尾向前逐格。返回是否找到路径 */
    private boolean bfsPath(int gx, int gy){
        int sx = World.toTile(unit.x), sy = World.toTile(unit.y);
        int start = pack(sx, sy), goal = pack(gx, gy);
        if(start == goal) return false;

        bfsQueue.clear();
        bfsDist.clear();
        bfsParent.clear();
        bfsDist.put(start, 0);
        bfsQueue.add(start);
        int head = 0;
        boolean found = false;

        while(head < bfsQueue.size){
            int cur = bfsQueue.get(head++);
            if(cur == goal){
                found = true;
                break;
            }
            int d = bfsDist.get(cur, -1);
            if(d >= pathLimit) continue;
            int cx = cur >> 16, cy = cur & 0xffff;
            for(int i = 0; i < 4; i++){
                int nx = cx + Geometry.d4x[i], ny = cy + Geometry.d4y[i];
                int key = pack(nx, ny);
                if(bfsDist.containsKey(key) || !passable(nx, ny)) continue;
                bfsDist.put(key, d + 1);
                bfsParent.put(key, cur);
                bfsQueue.add(key);
            }
        }

        if(!found) return false;

        // 回溯：从 goal 一路回到 start，按 goal→start 顺序存入 bfsPath
        bfsPath.clear();
        int node = goal;
        while(node != start){
            bfsPath.add(node);
            node = bfsParent.get(node, -1);
            if(node == -1) return false;
        }
        wpIndex = bfsPath.size - 1;
        return true;
    }

    /** 所在格是否可通行（范围内、非实心、单位可通过） */
    private boolean passable(int x, int y){
        if(!world.tiles.in(x, y)) return false;
        Tile t = world.tile(x, y);
        return t != null && !t.solid() && unit.canPass(x, y);
    }

    /** 打包格坐标为 int */
    private int pack(int x, int y){
        return x << 16 | (y & 0xffff);
    }

    /** 在当前点附近（3 格内）寻找敌人可见、可站立的位置 */
    private boolean findPeekPoint(Position aim, Vec2 out){
        float ang = unit.angleTo(aim);
        float[] rels = {0f, 40f, -40f, 80f, -80f, 120f, -120f, 180f};
        for(float rel : rels){
            for(float d = 1f * tilesize; d <= 3f * tilesize; d += tilesize){
                float px = unit.x + Angles.trnsx(ang + rel, d);
                float py = unit.y + Angles.trnsy(ang + rel, d);
                Tile t = world.tileWorld(px, py);
                if(t == null || t.solid() || !unit.canPass(t.x, t.y)) continue;
                if(!lineBlocked(px, py, aim)){
                    out.set(px, py);
                    return true;
                }
            }
        }
        return false;
    }

    /** 是否即将开火（可控武器装填将尽/已就绪待暖机）→ 驻停瞄准，避免弹道继承本体移动动量打偏 */
    private boolean fireHold(){
        for(var mount : unit.mounts){
            if(mount.weapon.controllable){
                float r = mount.reload;
                if(r > 0f && r <= 18f) return true;            // 装填将尽（约 0.3 秒内开火）
                if(r <= 0f && mount.warmup < 1f) return true;  // 已就绪但尚未开火（暖机中）→ 继续驻停
            }
        }
        return false;
    }

    /** 正常远程交战：无视线→绕墙找可见站位（不贴脸），射程外推进到射程边缘，射程内走位射击；近身交由逃离处理 */
    private void attackMove(Position aim){
        float dst = unit.dst(aim);
        boolean visible = !lineBlocked(unit.x, unit.y, aim);
        if(!visible){
            // 无视线（隔着墙）：绕墙到"能看见敌人"的交战距离边缘站位，绝不贴脸冲锋
            approachVisible(aim);
            unit.lookAt(aim);
        }else if(dst > weaponRange){
            // 射程外：推进到射程边缘
            plan = "ATTACK";
            planTarget.set(aim.getX(), aim.getY());
            planStopDist = weaponRange;
            movePathfind(aim.getX(), aim.getY(), weaponRange, unit.speed());
            unit.lookAt(aim);
        }else{
            // 射程内且可见：即将开火时驻停瞄准（避免弹道继承移动动量打偏），其余时间走位射击
            if(fireHold()){
                plan = "AIM";
                unit.lookAt(aim);
            }else{
                plan = "STRAFE";
                planTarget.set(aim.getX(), aim.getY());
                strafe(aim);
                unit.lookAt(aim);
            }
        }
    }

    /** 射程内走位：保持交战距离 + 朝提交的风筝锚点移动（锚点仅在到达/被挡/超时时更换），避免每帧乱换向 */
    private void strafe(Position aim){
        float dst = unit.dst(aim);
        // 径向修正：太近(<0.72 射程)→主要后退拉开距离；过远(>0.95 射程)→轻微前进；适中→纯横移
        float radial = 0f;
        if(dst < weaponRange * 0.72f) radial = 1f;
        else if(dst > weaponRange * 0.95f) radial = -0.35f;

        // 锚点需要更换的情形：无锚点 / 已到达 / 朝锚点方向被挡 / 定时到期
        boolean needNew = !hasKite || (kiteTimer -= Time.delta) <= 0f
            || unit.within(kitePoint.x, kitePoint.y, 2f)
            || blockedAhead(unit.angleTo(kitePoint));
        if(needNew){
            pickKite(aim, radial);
            kiteTimer = Mathf.random(35f, 70f);
        }

        // 朝风筝锚点移动（被墙挡用目标感知滑动，不随机乱窜）
        float dir = slideDir(unit.angleTo(kitePoint.x, kitePoint.y), unit.angleTo(aim));
        if(dir < 0f) return;
        if(tryDodge(dir)) return;           // 近处有敌弹 → 优先闪避
        tmp.trns(dir, unit.speed());
        unit.movePref(tmp);
    }

    /** 挑选新的风筝锚点：沿"横移+径向"合成方向前进 6 格，优先选更开阔一侧 */
    private void pickKite(Position aim, float radial){
        float toEnemy = unit.angleTo(aim);
        float side = Mathf.chance(0.5f) ? 1f : -1f;
        float o1 = openness(strafeDir(toEnemy, side, radial));
        float o2 = openness(strafeDir(toEnemy, -side, radial));
        if(o2 > o1) side = -side;
        float ang = strafeDir(toEnemy, side, radial);
        kitePoint.set(unit.x + Angles.trnsx(ang, 6f * tilesize), unit.y + Angles.trnsy(ang, 6f * tilesize));
        hasKite = true;
    }

    /** 合成走位方向：横向分量（side，±90°）与径向分量（radial，正=后退/负=前进）的矢量合成 */
    private float strafeDir(float toEnemy, float side, float radial){
        float ang = toEnemy + 90f * side;
        float dx = Angles.trnsx(ang, 1f) + Angles.trnsx(toEnemy + 180f, radial);
        float dy = Angles.trnsy(ang, 1f) + Angles.trnsy(toEnemy + 180f, radial);
        return Mathf.atan2(dy, dx);
    }

    /** 走位/换弹途中整合闪避：正在闪避→继续移动；否则按冷却/扫描节奏检测敌弹并触发。返回 true 表示已接管移动 */
    private boolean tryDodge(float preferred){
        if(dodgeMove > 0f){
            dodgeMove -= Time.delta;
            tmp.trns(dodgeDir, unit.speed() * 1.15f);
            unit.movePref(tmp);
            return true;
        }
        if((dodgeCd -= Time.delta) > 0f) return false;
        if((dodgeScan -= Time.delta) > 0f) return false;
        dodgeScan = dodgeScanInterval;
        if(findThreat()){
            dodgeCd = dodgeCooldown;
            dodgeMove = dodgeMoveTime;
            dodgeDir = threatDodgeDir(preferred);
            tmp.trns(dodgeDir, unit.speed() * 1.15f);
            unit.movePref(tmp);
            return true;
        }
        return false;
    }

    /** 附近是否有敌方子弹正朝本单位飞来（弹道方向与"子弹→自身"夹角 < dodgeAngle），记录最近威胁到 threatPos */
    private boolean findThreat(){
        threatPos.set(Float.NaN, Float.NaN);
        final float[] best = {Float.MAX_VALUE};
        Groups.bullet.intersect(unit.x - dodgeRadius, unit.y - dodgeRadius, dodgeRadius * 2f, dodgeRadius * 2f, b -> {
            if(b.team != unit.team && b.type.hittable && b.within(unit.x, unit.y, dodgeRadius)
                && Angles.angleDist(b.rotation(), b.angleTo(unit.x, unit.y)) < dodgeAngle){
                float d = b.dst(unit.x, unit.y);
                if(d < best[0]){
                    best[0] = d;
                    threatPos.set(b.x(), b.y());
                }
            }
        });
        return !Float.isNaN(threatPos.x);
    }

    /** 闪避方向：垂直于威胁来袭方向（threatPos→自身），取与 preferred 更接近的一侧 */
    private float threatDodgeDir(float preferred){
        float in = Mathf.atan2(unit.y - threatPos.y, unit.x - threatPos.x);
        float a = in + 90f, b = in - 90f;
        return Angles.angleDist(a, preferred) <= Angles.angleDist(b, preferred) ? a : b;
    }

    /** 沿方向前方是否被墙/不可通行阻挡 */
    private boolean blockedAhead(float ang){
        float nx = unit.x + Angles.trnsx(ang, unit.hitSize + 2f);
        float ny = unit.y + Angles.trnsy(ang, unit.hitSize + 2f);
        Tile t = world.tileWorld(nx, ny);
        return t == null || t.solid() || !unit.canPass(t.x, t.y);
    }

    /** 沿方向的可通行开阔度（采样前方若干格，越大越开阔） */
    private float openness(float ang){
        float score = 0f;
        for(float d = 1f * tilesize; d <= 3f * tilesize; d += tilesize){
            float nx = unit.x + Angles.trnsx(ang, d);
            float ny = unit.y + Angles.trnsy(ang, d);
            Tile t = world.tileWorld(nx, ny);
            if(t == null || t.solid() || !unit.canPass(t.x, t.y)) break;
            score += 1f;
        }
        return score;
    }

    /** 沿方向前进，被墙挡时沿"更接近目标"的一侧滑墙；两侧都挡则全向找"兼顾开阔与目标方向"的滑出方向；完全无路返回 -1 */
    private float slideDir(float dir, float goalDir){
        if(!blockedAhead(dir)) return dir;
        float dl = openness(dir + 90f), dr = openness(dir - 90f);
        if(dl > 0f || dr > 0f){
            // 两侧都开：选与目标方向更一致的一侧（避免绕远路/墙角来回蹭）
            if(dl > 0f && dr > 0f){
                return Angles.angleDist(dir + 90f, goalDir) <= Angles.angleDist(dir - 90f, goalDir) ? dir + 90f : dir - 90f;
            }
            return dl > 0f ? dir + 90f : dir - 90f;
        }
        // 左右都被挡：全向找"开阔且朝目标"的方向滑出（防卡死，且不随机乱窜）
        float best = dir, bestScore = -1f;
        for(int a = 0; a < 360; a += 45){
            float o = openness(a);
            if(o > 0f){
                float score = o + (1f - Angles.angleDist(a, goalDir) / 180f);
                if(score > bestScore){
                    bestScore = score;
                    best = a;
                }
            }
        }
        return bestScore > 0f ? best : -1f;
    }

    /** 强制武器瞄准与 AI 锁定目标一致：
     *  rotate=false 的弹道沿 unit.rotation（身体朝向）发射，开火门控要求 unit.rotation ≈ angleTo(aim)。
     *  若不强制，被夹击时引擎 updateWeapons 会让武器锁到身后的敌人、而身体面朝身前 → 门控差 ~180° 永远打不出。
     *  让 mount.target/aim 指向 lockedTarget 当前位置（身体正对的目标），门控恒与朝向一致。 */
    @Override
    public void updateWeapons(){
        super.updateWeapons();
        if(lockedTarget != null && lockedTarget instanceof Teamc tc){
            for(var mount : unit.mounts){
                if(mount.weapon.controllable){
                    mount.target = tc;
                    mount.aimX = lockedTarget.getX();
                    mount.aimY = lockedTarget.getY();
                    // 严格限制开火范围：锁定目标必须在交战距离（weaponRange=16格）内，
                    // 否则引擎按 bullet.range+余量 判定会在射程外空射
                    mount.shoot &= unit.within(lockedTarget, weaponRange);
                }
            }
        }
    }

    /** 看不到敌人就不射击（隔着墙/掩体不空射、不穿墙），需探出掩体或绕到有视线处才能开火 */
    @Override
    public boolean shouldFire(){
        if(lockedTarget != null && lineBlocked(unit.x, unit.y, lockedTarget)){
            return false;
        }
        return super.shouldFire();
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

    // —— 调试：地图悬浮文本 + 终端计划日志（debug=true 时启用） ——

    /** 按节奏刷新地图文本与终端日志；计划标签变化时立即打印详情 */
    private void debugStep(){
        if(!debug) return;
        if((debugTimer -= Time.delta) <= 0f){
            debugTimer = debugInterval;
            debugDraw();
        }
        if((logTimer -= Time.delta) <= 0f){
            logTimer = logInterval;
            debugLog(false);
        }
        if(!plan.equals(lastLogPlan)){
            lastLogPlan = plan;
            debugLog(true);
        }
    }

    /** 地图悬浮文本：状态（单位头顶）/ 计划目标 / 寻路路径路点 */
    private void debugDraw(){
        if(headless || !debug) return;
        float dur = debugInterval / 60f + 0.15f;
        // 状态
        Call.label(plan, dur, unit.x, unit.y + unit.hitSize + 2f);
        // 计划目标
        Call.label("目标(" + Mathf.round(planTarget.x / tilesize) + "," + Mathf.round(planTarget.y / tilesize) + ")", dur, planTarget.x, planTarget.y + 8f);
        // 寻路路径路点（从起点侧向前画，最多 12 个）
        if(hasPath && bfsPath.size > 0){
            int show = Math.min(bfsPath.size, 12);
            for(int i = 0; i < show; i++){
                int idx = bfsPath.size - 1 - i;
                if(idx < 0) break;
                Call.label(i == 0 ? "P" : "" + i, dur, wpX(idx), wpY(idx));
            }
        }
    }

    /** 终端计划详情：planChanged=true 打印计划变更，否则打印周期心跳 */
    private void debugLog(boolean planChanged){
        if(!debug) return;
        String head = planChanged ? "[Ambusher 计划变更]" : "[Ambusher 心跳]";
        Log.info(head + " 状态=" + plan +
            " 目标格=(" + World.toTile(planTarget.x) + "," + World.toTile(planTarget.y) + ")" +
            " 停止距离=" + Mathf.round(planStopDist / tilesize * 10) / 10f + "格" +
            " 距目标=" + Mathf.round(unit.dst(planTarget) / tilesize * 10) / 10f + "格" +
            " 路径步数=" + bfsPath.size + " 当前路点=" + wpIndex +
            (lockedTarget != null ? " 锁定目标=" + targetDesc(lockedTarget) : " 无锁定目标"));
    }

    /** 目标简述（调试用） */
    private String targetDesc(Position t){
        if(t instanceof Unit u) return "单位#" + u.id + "@" + Mathf.round(u.dst(unit) / tilesize * 10) / 10f + "格";
        if(t instanceof Building b) return "建筑@" + b.block.name;
        return "位置";
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
        //    先找"当前射程内且视线通"的目标：弩手优先打能马上命中的（箭不浪费在打不到的墙上）；
        //    其次找最近的普通单位；敌方持盾先锋的盾牌（Shield）会吸收弩箭，仅在无其它目标时兜底
        Unit inRange = Units.closestEnemy(unit.team, unit.x, unit.y, weaponRange,
            u -> u.checkTarget(true, true) && !(u instanceof Shield) && !lineBlocked(unit.x, unit.y, u));
        if(inRange != null) return inRange;
        Unit enemy = Units.closestEnemy(unit.team, unit.x, unit.y, detectRange,
            u -> u.checkTarget(true, true) && !(u instanceof Shield));
        if(enemy != null) return enemy;
        Unit any = Units.closestEnemy(unit.team, unit.x, unit.y, detectRange, u -> u.checkTarget(true, true));
        if(any != null) return any;

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
