package endfieldindustrylib.EFworld.unit.Landbreakers;

import arc.math.Angles;
import arc.math.Mathf;
import endfieldindustrylib.EFcontents.EFunits;
import endfieldindustrylib.EFworld.ai.VanguardAI;
import static mindustry.Vars.tilesize;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.EntityMapping;
import mindustry.gen.Groups;
import mindustry.gen.MechUnit;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import mindustry.type.Weapon;

/**
 * 先锋（Vanguard）专用实体 — 管理盾牌单位（新建单位）的生成、正面跟随与脱落。
 * <p>
 * <ul>
 *   <li>单位生成时（{@link #add}）创建一面 {@link VanguardShield} 盾牌</li>
 *   <li>存档载入时通过 {@link #shieldId} 找回已保存的盾牌，避免重复生成孤儿盾</li>
 *   <li>每帧把盾牌驱动到本体正前方（{@code rotation} 方向），朝向与本体一致
 *       → 从正面来的子弹被盾牌物理命中吸收；<b>正面近战</b>由 {@link #damage} 检测伤害来源后转发给盾牌</li>
 *   <li>本体移除/死亡时盾牌一并移除</li>
 *   <li>{@link #dropShield()} 让盾牌脱落消失（预留给"物理效果"异常 TODO 触发）</li>
 * </ul>
 */
public class Vanguard extends MechUnit{

    /**
     * 实体唯一注册 id：通过 {@link EntityMapping#register} 注册本类到实体映射，
     * 使存档/网络按此 id 反序列化回 {@code VanguardUnit} 而非基类 {@code MechUnit}。
     */
    public static final int ENTITY_ID = EntityMapping.register("endfield-industry-lib-vanguard-unit", Vanguard::new);

    @Override
    public int classId(){
        return ENTITY_ID;
    }

    /** 盾牌实体引用（可为 null） */
    public Shield shieldUnit;
    /** 盾牌实体 id（序列化用）：存档载入后据此找回已存在的盾牌，避免重复生成 */
    public int shieldId = -1;
    /** 是否仍持盾（脱落/移除后为 false） */
    public boolean hasShield = true;
    /** 盾牌与本体中心的距离（世界单位） */
    private static final float SHIELD_DIST = 7f;
    /** 持盾减伤比例（0.5 = 拥有盾牌时本体受到的伤害减半） */
    private static final float SHIELD_DAMAGE_REDUCTION = 0.5f;
    /** 正面近战判定：攻击者贴身距离（世界单位，4 格，覆盖本族近战 splash 半径） */
    private static final float MELEE_RANGE = 4f * tilesize;
    /** 正面判定圆锥半角（度）：相对盾牌朝向（rotation）的夹角，盾牌覆盖的正面范围 */
    private static final float FRONT_CONE = 75f;

    @Override
    public void add(){
        super.add();
        // 全新生成时创建盾牌；存档载入（shieldId 已还原）时不生成，由 afterReadAll 找回，
        // 避免载入后多出一面孤儿盾牌
        if(shieldId == -1 && hasShield){
            spawnShield();
        }
    }

    /** 存档载入完成后：找回保存的盾牌（若仍存在），否则在仍应持盾时补一面 */
    @Override
    public void afterReadAll(){
        super.afterReadAll();
        if(shieldId != -1){
            if(Groups.unit.getByID(shieldId) instanceof Shield su && su.isAdded() && !su.dead){
                shieldUnit = su;
                hasShield = true;
            }else{
                // 存档中的盾牌缺失 → 重置引用并补一面
                shieldId = -1;
                if(hasShield){
                    spawnShield();
                }
            }
        }
    }

    /** 创建盾牌单位并挂到本体正面 */
    private void spawnShield(){
        float dir = rotation;   // 主体朝向（与长矛方向一致，而非 baseRotation）
        Unit s = EFunits.vanguardShield.spawn(team, x + Angles.trnsx(dir, SHIELD_DIST), y + Angles.trnsy(dir, SHIELD_DIST));
        if(s != null && s instanceof Shield su){
            su.rotation = dir;   // 生成时即与主体朝向（rotation）一致
            shieldUnit = su;
            shieldId = su.id();
        }else if(s != null){
            s.remove();   // 类型不符则清理（防御性）
        }
    }

    @Override
    public void update(){
        super.update();

        // 盾牌被打碎/移除：失去护盾（持盾减伤随之消失）
        if(hasShield && shieldUnit != null && (!shieldUnit.isAdded() || shieldUnit.dead)){
            shieldUnit = null;
            shieldId = -1;
            hasShield = false;
        }

        // 每帧驱动盾牌跟随本体正面（朝向 = 主体 rotation，与长矛方向一致，而非 baseRotation）；
        // 位置叠加与 UnitType.draw 中 Mech 身体 legOffset 相同的走路晃动偏移 → 盾牌随身体同步晃动
        if(hasShield && shieldUnit != null && shieldUnit.isAdded()){
            float dir = rotation;
            float side = Mathf.lerp(Mathf.sin(walkExtend(true), 2f/Mathf.PI, 1f) * type.mechSideSway, 0f, elevation);
            float front = Mathf.lerp(Mathf.sin(walkExtend(true), 1f/Mathf.PI, 1f) * type.mechFrontSway, 0f, elevation);
            float a = baseRotation();
            shieldUnit.set(
                x + Angles.trnsx(dir, SHIELD_DIST) + Angles.trnsx(a, 0f, side) + Angles.trnsx(a + 90, 0f, front),
                y + Angles.trnsy(dir, SHIELD_DIST) + Angles.trnsy(a, 0f, side) + Angles.trnsy(a + 90, 0f, front)
            );
            shieldUnit.rotation = dir;
            shieldUnit.vel.setZero();
        }
    }

    /** 持盾减伤：拥有盾牌时本体受到的伤害减半（50% 减伤）；
     *  正面近战伤害直接转发给盾牌（消耗盾牌耐久，本体不扣血、不触发 50% 减伤） */
    @Override
    public void damage(float amount){
        // 正面近战：检测到盾牌朝向正前方有贴身敌方攻击者 → 把伤害转发给盾牌
        if(hasShield() && shieldUnit != null && shieldUnit.isAdded() && frontMeleeAttacker() != null){
            shieldUnit.damage(amount);
            return;
        }
        if(hasShield()){
            super.damage(amount * (1f - SHIELD_DAMAGE_REDUCTION));
        }else{
            super.damage(amount);
        }
    }

    /** 检测正前方的近战伤害来源：返回一个位于盾牌朝向（rotation）前方圆锥内、且贴身距离内的敌方单位。
     *  damage() 不携带来源参数，只能从攻击者位置推断（远程单位不会贴身到 4 格内） */
    private Unit frontMeleeAttacker(){
        final Unit[] best = {null};
        final float[] bestD = {Float.MAX_VALUE};
        Units.nearbyEnemies(team, x, y, MELEE_RANGE, u -> {
            if(u.isValid() && !u.dead){
                float d = u.dst(x, y);
                if(d < MELEE_RANGE && d < bestD[0] && Angles.angleDist(rotation, angleTo(u)) < FRONT_CONE){
                    bestD[0] = d;
                    best[0] = u;
                }
            }
        });
        return best[0];
    }

    /** 盾牌脱落：播放脱落粒子并移除盾牌（预留给"物理效果"异常 TODO 触发） */
    public void dropShield(){
        if(shieldUnit != null && shieldUnit.isAdded()){
            shieldUnit.detach();
        }
        shieldUnit = null;
        shieldId = -1;
        hasShield = false;
    }

    /** 是否仍持盾（供 AmbusherAI 判断"友军持盾者是否为掩体"） */
    public boolean hasShield(){
        return hasShield && shieldUnit != null;
    }

    @Override
    public void remove(){
        // 本体移除时，盾牌一并移除（避免残留孤立盾牌）
        if(shieldUnit != null && shieldUnit.isAdded()){
            shieldUnit.remove();
        }
        shieldUnit = null;
        shieldId = -1;
        super.remove();
    }

    public static Vanguard create(){
        return new Vanguard();
    }
    public static class VanguardType extends UnitType{

    public VanguardType(String name){
        super(name);

        // —— 基础属性：盾戳（T1，比 Raider 更硬更慢） ——
        health = 300f;
        speed = 0.5f;
        hitSize = 10f;
        armor = 0f;
        drag = 0.4f;
        accel = 0.3f;
        rotateSpeed = 3.5f;

        // —— 地面单位 ——
        flying = false;
        physics = true;                 // 启动物理碰撞
        hovering = false;               // 不悬浮，受地面影响
        canDrown = true;                // 可在深水中淹死
        canBoost = false;               // 不能起飞
        omniMovement = true;            // 可原地转向：朝按键/目标方向直行，身体转向移动方向（人形）
        isEnemy = true;
        drawBody = true;
        drawCell = false;
        drawSoftShadow = false;
        drawItems = true;
        targetAir = false;              // 近战仅对地面

        // —— 人形机甲（Mech）参数 ——
        constructor = Vanguard::create;
        mechSideSway = 0.45f;           // 行走左右摆动
        mechFrontSway = 0.3f;
        baseRotateSpeed = 8f;           // 身体转向较快（举盾转向灵敏）；mechStride 由引擎按 hitSize 自动计算

        // —— 武器：右手长矛戳刺（近战，范围较远单点） ——
        weapons.add(new Weapon("endfield-industry-lib-vanguard-spear"){{
            mirror = false;            // 单个长矛，不镜像成对
            top = false;               // 画在身体之下（长矛在主贴图之下绘制）
            layerOffset = -0.05f;      // 负层级：渲染在身体/盾牌之下
            x = 5f; y = 0f;            // 右手位置（右前方）
            shootY = 8f;               // 矛尖
            rotate = false;            // 固定朝前：Mech 身体（baseRotation）朝敌人即命中
            controllable = false;
            autoTarget = true;         // 自动索敌戳刺
            reload = 120f;              // 戳刺频率（帧）
            shootCone = 45f;
            recoil = -5f;
            shake = 0.5f;
            bullet = new BulletType(0f, 0f){{
                speed = 0f;
                lifetime = 1f;
                instantDisappear = true;   // 立即戳刺
                splashDamage = 24f;        // 戳刺伤害
                splashDamageRadius = 20f;  // 单点戳（范围较小、集中）
                collidesAir = false;       // 仅攻击地面目标
                collidesGround = true;
                hittable = false;
                rangeOverride = 9f;        // 攻击/发现距离（与 AI 的贴近距离匹配，避免远处空挥）
            }};
        }});

        // —— AI：近战贴近戳刺（盾戳），盾牌由实体独立驱动 ——
        aiController = () -> new VanguardAI();
    }
}

}
