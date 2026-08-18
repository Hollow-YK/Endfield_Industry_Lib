package endfieldindustrylib.EFworld.ai;

import arc.math.geom.Position;

/**
 * 重装拉姆（Heavyram）专属 AI — 在 {@link RamAI} 基础上新增「冲锋」技能。
 * <p>
 * 普通状态沿用 RamAI 的走路索敌 + 贴身近战；当索敌到目标且满足冲锋条件（距离适中、直线可通行、
 * 内置冷却结束）时：先<strong>锁定敌人原地停顿蓄力</strong>，随后切换为疾驰奔跑步态高速冲向
 * 目标，<strong>直到靠近敌人</strong>（进入命中距离即停），再回到普通走路攻击。两次冲锋之间
 * 的内置冷却为 20 秒。
 * <p>
 * 冲锋状态机与全部参数集中在 {@link HeavyramCharge}，供本 AI 与 {@link RamCommandAI}（玩家指挥）共用。
 */
public class HeavyramAI extends RamAI {
    /** 冲锋技能逻辑（蓄力 → 疾驰冲锋） */
    private final HeavyramCharge charge = new HeavyramCharge();

    @Override
    public void updateMovement(){
        Position nextTarget = findTarget();

        if(nextTarget == null){
            // 无目标：取消蓄力/冲锋，走路寻路
            charge.stop(unit);
            walkToCore();
            kickDust();
            return;
        }

        if(charge.update(unit, nextTarget)){
            // 蓄力/冲锋中：由冲锋逻辑接管移动；冲锋中扬尘
            if(charge.charging()){
                kickDust();
            }
        }else{
            // 普通走路索敌攻击
            attackMove(nextTarget);
            kickDust();
        }
    }
}

