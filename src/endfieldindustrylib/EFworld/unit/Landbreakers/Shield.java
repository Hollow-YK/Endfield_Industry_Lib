package endfieldindustrylib.EFworld.unit.Landbreakers;

import static mindustry.Vars.headless;
import mindustry.ai.types.GroundAI;
import mindustry.content.Fx;
import mindustry.gen.EntityMapping;
import mindustry.gen.UnitEntity;
import mindustry.graphics.Layer;
import mindustry.type.UnitType;

/**
 * 先锋（Vanguard）的盾牌实体 — 独立单位，位于先锋正前方，吸收正面伤害。
 * <p>
 * 机制：
 * <ul>
 *   <li>位置/朝向每帧由父 {@link Vanguard} 驱动到本体正面（自身不移动）</li>
 *   <li>盾牌正常受伤（325 血 / 15 甲，护甲在伤害结算时自动生效）：正面子弹/近战命中
 *       盾牌被挡下并消耗盾牌耐久；盾牌被打碎（死亡）后由父实体检测清理</li>
 *   <li>本体持盾期间享受 50% 减伤（见 {@link Vanguard#damage}）；盾碎后减伤消失</li>
 *   <li>"物理效果"异常（TODO）触发 {@link Vanguard#dropShield()} 时直接脱落消失</li>
 * </ul>
 */
public class Shield extends UnitEntity{

    /**
     * 实体唯一注册 id：通过 {@link EntityMapping#register} 注册本类到实体映射，
     * 使存档/网络按此 id 反序列化回 {@code ShieldUnit} 而非基类 {@code UnitEntity}。
     */
    public static final int ENTITY_ID = EntityMapping.register("endfield-industry-lib-vanguard-shield-unit", Shield::new);

    @Override
    public int classId(){
        return ENTITY_ID;
    }

    @Override
    public void update(){
        super.update();
        // 不自主移动：位置由父 VanguardUnit 每帧 set；此处仅稳住速度，防止物理/动画漂移
        vel.setZero();
    }

    @Override
    public void damage(float amount){
        // 盾牌正常受伤（325 血 / 15 甲）：被打碎（死亡）后由父 VanguardUnit 检测清理，
        // Vanguard 随即失去持盾减伤。
        super.damage(amount);
        // TODO: 计划新建的"物理效果"异常（暂留）——受到其影响时由 VanguardUnit.dropShield() 让盾牌直接脱落（不消耗耐久）
    }

    /** 盾牌脱落：播放脱落粒子并从世界移除（由 VanguardUnit.dropShield 调用） */
    public void detach(){
        if(!headless){
            Fx.smoke.at(x, y);
            Fx.breakProp.at(x, y);
        }
        remove();
    }

    public static Shield create(){
        return new Shield();
    }
    public static class VanguardShieldType extends UnitType{

    public VanguardShieldType(String name){
        super(name);
        health = 325f;
        armor = 12f;
        hidden = true;            // 不显示在核心数据库 / 各类 UI（世界内仍正常渲染）
        hitSize = 10f;             // 碰撞箱改薄：Mindustry 单位命中恒为以 hitSize 为半径的圆，无法原生用矩形；
                                  // 缩小 hitSize 让命中范围变成一条窄线（正面直射被挡，侧面子弹不再被大片吸收）
        // 地面单位（可被对地子弹/近战命中 → 吸收伤害；攻击伤害在 ShieldUnit.damage 拦截）
        flying = false;
        physics = false;          // 不参与物理推挤（位置由父先锋每帧强制驱动）
        hovering = false;
        canDrown = false;
        canBoost = false;
        omniMovement = true;
        isEnemy = true;
        targetAir = false;
        targetGround = false;
        logicControllable = false;  // 不可被逻辑控制
        playerControllable = false; // 不可被玩家控制
        targetable = false;       
        drawBody = true;
        drawCell = false;
        drawItems = false;
        drawSoftShadow = false;
        groundLayer = 62;
        useUnitCap = false;       // 不占用单位容量
        allowedInPayloads = false; // 载荷源/载荷系统无法装载、搬运或产出盾牌（盾牌只由先锋生成驱动）

        // 无武器：纯盾牌（不攻击），由父先锋驱动
        constructor = Shield::create;
        // 空 AI：不自主移动（位置/朝向每帧由父 VanguardUnit 强制驱动，此处仅防止默认寻路移动）
        aiController = () -> new GroundAI(){
            @Override
            public void updateMovement(){
                // 盾牌不移动
            }
        };
    }
}

}
