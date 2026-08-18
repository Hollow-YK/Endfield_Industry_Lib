package endfieldindustrylib.EFworld.ai;

import arc.graphics.Color;
import arc.math.geom.Position;
import arc.util.Time;
import static mindustry.Vars.headless;
import static mindustry.Vars.tilesize;
import mindustry.ai.types.GroundAI;
import mindustry.content.Fx;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.world.blocks.environment.Floor;

/**
 * 刺蝎（Sting）专属 AI — 与 RamAI 结构一致，但为远程攻击（蝎尾炮台）。
 * <p>
 * 索敌与 RamAI 完全相同（单位 > 炮台 > 建筑 > 墙体），且可对空（与蝎尾炮台
 * collidesAir/collidesGround 一致）。发现目标后走位：
 * <ul>
 *   <li>目标在武器射程外 → 向目标推进，进入射程即停</li>
 *   <li>目标贴脸（小于安全距离）→ 后退拉开距离</li>
 *   <li>目标在射程内 → 原地停住，交给尾部炮台（autoTarget）自动索敌开火</li>
 * </ul>
 * 无目标时交由 {@link GroundAI} 标准逻辑朝核心寻路。
 */
public class StingAI extends GroundAI {
    /** 索敌半径（世界单位）：20 格 */
    private static final float detectRange = 20f * tilesize;
    /** 武器射程（世界单位，与蝎尾炮台子弹射程 96 匹配）：12 格 */
    private static final float weaponRange = 12f * tilesize;
    /** 贴脸安全距离（世界单位）：目标过近时后退拉开 */
    private static final float minRange = 5f * tilesize;
    /** 推进/后撤的平滑步长 */
    private static final float attackSmooth = 5f;
    /** 奔跑扬尘计时（帧） */
    private float dustTimer = 0f;

    @Override
    public void updateMovement(){
        // —— 索敌：按仇恨优先级取目标，否则朝核心寻路（标准 GroundAI） ——
        Position nextTarget = findTarget();

        if(nextTarget != null){
            attackMove(nextTarget);
        }else{
            super.updateMovement();
        }

        kickDust();
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
        // 1. 单位优先（含空中，与蝎尾炮台 collidesAir 一致）
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

    /** 远程攻击走位：射程外推进、贴脸后退、射程内原地射击（尾部炮台自动开火） */
    private void attackMove(Position aim){
        float dst = unit.dst(aim);

        if(dst > weaponRange-0.1f){
            // 射程外：向目标推进，到达射程边缘即停
            moveTo(aim, weaponRange, attackSmooth);
        }else if(dst < minRange){
            // 贴脸：后退拉开到安全距离（keepDistance=true → 越过圆环时反向移动）
            moveTo(aim, minRange, attackSmooth, true, null);
        }
        // 射程内：保持原地，蝎尾炮台（autoTarget）自动索敌射击

        // 面向目标：尾部炮台独立旋转，此朝向仅用于走位观感
        unit.lookAt(aim);
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
}
