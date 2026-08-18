package endfieldindustrylib.EFworld.unit;

import arc.Events;
import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.math.geom.Rect;
import arc.struct.IntMap;
import arc.struct.Seq;
import arc.util.Time;
import endfieldindustrylib.EFcontents.EFunits;
import mindustry.ai.UnitCommand;
import mindustry.content.Fx;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.game.EventType.UnitDestroyEvent;
import mindustry.game.EventType.WorldLoadEvent;
import mindustry.gen.EntityMapping;
import static mindustry.gen.Sounds.explosionReactor;
import mindustry.gen.TankUnit;
import mindustry.gen.Tankc;
import mindustry.gen.Unit;
import mindustry.type.UnitType;


/**
 * 塔塔——所有战役关卡中需要护送的单位。
 * <p>
 * 塔塔是地面单位，血量较高，移速极慢，无武器但有建造能力，
 * 且是唯一可以拆除"侵蚀核"的单位（侵蚀核的逻辑检查塔塔的类型）。
 * 定义从原 EFunits 内联块迁移至此。
 */
public class Tata extends UnitType {

    public Tata(String name) {
        super(name);

        // —— 基础属性 ——
        health = 2500f;
        speed = 0.15f;
        hitSize = 24f;
        armor = 2f;
        drag = 0.4f;
        accel = 0.3f;
        rotateSpeed = 2f;
        // —— 地面单位 ——
        flying = false;
        physics = true;                 // 启动物理碰撞
        hovering = false;               // 不悬浮，受地面影响
        canDrown = true;                // 可在深水中淹死
        canBoost = false;               // 不能起飞
        omniMovement = true;            // 需转向移动，产生转头效果
        faceTarget = true;               // 面向移动方向                
        // —— 建造能力 ——
        buildSpeed = 4.0f;
        buildRange = 240f;

        // —— 游戏属性 ——
        isEnemy = false;
        playerControllable = true;
        logicControllable = true;
        useUnitCap = true;
        hoverable = true;
        // —— 命令模式 ——
        commands = Seq.with(
            UnitCommand.moveCommand,
            UnitCommand.rebuildCommand,
            UnitCommand.assistCommand,
            EFunits.followCommand
        );
        defaultCommand = UnitCommand.moveCommand;

        // —— 显示 ——
        drawSoftShadow=false;
        drawBody = true;
        drawCell = false;
        drawItems = true;

        // 使用塔塔专用实体（下方内嵌静态类 TataUnit）：
        // 地形碰撞箱与碰撞箱一致（完全不能走上地形）、质量极大（无法被任何单位推动）
        constructor = TataUnit::create;
    }

    /** 塔塔贴图整体放大 50% 的绘制缩放系数 */
    private static final float SPRITE_SCL = 1.5f;
    /** 头部转向速度（度/秒） */
    private static final float BODY_ROTATE_SPEED = 5f;
    /** 履带转向速度（度/秒）：驱动时向实际移动方向转动，明显快于头部 */
    private static final float HULL_ROTATE_SPEED = 3f;

    // —— 每单位“履带/头部”两个旋转角，按 unit.id 存于静态表 ——
    // float[0] = hullRot（履带朝向），float[1] = bodyRot（头部朝向）
    private static final IntMap<float[]> rotState = new IntMap<>();
    private static boolean rotCleanupRegistered = false;

    private static void registerRotCleanup(){
        if(rotCleanupRegistered) return;
        rotCleanupRegistered = true;
        // 单位销毁 / 世界重载时清理状态，避免残留或 id 复用串状态
        Events.on(UnitDestroyEvent.class, e -> rotState.remove(e.unit.id));
        Events.on(WorldLoadEvent.class, e -> rotState.clear());
    }

    private static float[] rotOf(Unit unit){
        float[] r = rotState.get(unit.id);
        if(r == null){
            // 首次：直接对齐当前朝向，避免生成时甩头
            r = new float[]{unit.rotation, unit.rotation};
            rotState.put(unit.id, r);
        }
        return r;
    }

    /**
     * 覆写 draw：使塔塔的全部贴图放大 50%。
     * <p>
     * 注意：引擎默认的 draw 流程中，drawOutline/drawBody/drawCell 内部会调用
     * {@link Draw#reset()} 把 xscl/yscl 重置回 1，因此只包裹 super.draw 无法放大
     * 车身本体。这里把 draw / drawBody / drawCell 分别按“进入时的缩放 ×1.5”包裹，
     * 且每次进入子绘制前缩放已被内部 reset 归一化，不会重复叠加放大。
     */
    @Override
    public void draw(Unit unit){
        float px = Draw.xscl, py = Draw.yscl;
        Draw.scl(px * SPRITE_SCL, py * SPRITE_SCL);
        super.draw(unit);
        Draw.scl(px, py);
    }

    /**
     * 头部（车身）用每帧维护的朝向（按 unit.id 存于 {@link #rotState} 的 bodyRot）绘制。
     * bodyRot 始终缓慢逼近“看向的方向”（{@code prefRotation()}：建造时看向建造目标、
     * 移动时看向移动方向、空闲时保持），因此即使不移动也能单独转头“看向”某方向；
     * 履带（hullRot）在静止时保持冻结。见 {@link #update(Unit)}。
     */
    @Override
    public void drawBody(Unit unit){
        float px = Draw.xscl, py = Draw.yscl;
        Draw.scl(px * SPRITE_SCL, py * SPRITE_SCL);
        float[] r = rotState.get(unit.id);
        if(r != null){
            applyColor(unit);
            Draw.rect(region, unit.x, unit.y, r[1] - 90);
            Draw.reset();
        }else{
            super.drawBody(unit);
        }
        Draw.scl(px, py);
    }

    /**
     * 履带单独处理，两点：
     * <ol>
     *   <li>塔塔有建造能力，引擎在画履带（drawTank）之前会先调用 {@code unit.drawBuilding()}，
     *       其中 {@code drawBuildingBeam()} 末尾的 {@link Draw#reset()} 会把 xscl/yscl 重置回 1，
     *       导致随后绘制的履带贴图恢复成原始大小。这里在画履带前强制恢复放大倍率（绝对 1.5，
     *       避免相对缩放重复放大）。</li>
     *   <li>履带用每帧维护的 hullRot（按 unit.id 存于 {@link #rotState}）绘制：仅在实际移动
     *       （驱动）时向移动方向转动，静止时冻结——不随头部“看向”某方向而转动。见 {@link #update(Unit)}。</li>
     * </ol>
     */
    @Override
    public <T extends Unit & Tankc> void drawTank(T unit){
        float px = Draw.xscl, py = Draw.yscl;
        Draw.scl(SPRITE_SCL, SPRITE_SCL);
        float[] r = rotState.get(unit.id);
        if(r != null){
            // 临时用 hullRot 替代 unit.rotation 画履带，画完立即恢复
            float old = unit.rotation;
            unit.rotation = r[0];
            super.drawTank(unit);
            unit.rotation = old;
        }else{
            super.drawTank(unit);
        }
        Draw.scl(px, py);
    }

    /**
     * 每帧更新“履带/头部”两个独立部件的朝向。放在 UnitType 层，
     * 保证对每个塔塔实例都执行（经 {@code type.update(self())} 调用，与实体是否为自定义实体无关）。
     * 每单位状态按 {@code unit.id} 存于 {@link #rotState}。
     * <ul>
     *   <li>履带：仅在单位“实际发生位移/在移动”时向<b>实际移动方向</b>转动，静止时冻结；
     *       用 deltaLen / vel 方向而非 unit.rotation（玩家操控下 rotation 更新极慢且不可靠）。</li>
     *   <li>头部：始终缓慢向“看向的方向”（{@code prefRotation()}：建造目标/移动方向/当前朝向）旋转。</li>
     * </ul>
     */
    @Override
    public void update(Unit unit){
        registerRotCleanup();
        float[] r = rotOf(unit);
        // 履带：仅在“实际发生位移/在移动”时向“实际移动方向”转动，静止时冻结
        if(unit.deltaLen() > 0.01f || unit.moving()){
            r[0] = Angles.moveToward(r[0], unit.vel().angle(), HULL_ROTATE_SPEED * Time.delta);
        }
        // 头部：始终向“看向的方向”缓慢旋转
        r[1] = Angles.moveToward(r[1], unit.prefRotation(), BODY_ROTATE_SPEED * Time.delta);
    }

    // —— 死亡爆炸（等同于钍反应堆） ——
    @Override
    public void killed(Unit unit) {
        Fx.reactorExplosion.at(unit.x, unit.y);
        Effect.shake(6f, 16f, unit);
        Damage.damage(null, unit.x, unit.y, 19f * 8f, 5000f, true, true, true);
        explosionReactor.at(unit, 1f);
    }

    /**
     * 塔塔专用实体（内嵌静态类，无需额外文件）。
     * <ul>
     *   <li><b>完全不能走上地形：</b>引擎默认地形碰撞箱为 {@code min(hitSize*0.66, 7.8)}，
     *       小于碰撞箱，因此坦克可“部分压上”地形；这里令地形碰撞箱与碰撞箱（{@code hitSize}）一致，
     *       塔塔将被墙体/地形完全挡住。</li>
     *   <li><b>质量极大：</b>单位间物理推动按质量占比分配（{@code PhysicsProcess}），
     *       质量设为 1e12 后塔塔无法被任何单位推动，只会把其他单位挤开。</li>
     * </ul>
     */
    public static class TataUnit extends TankUnit {

        /** 实体唯一注册 id：存档/网络据此反序列化回本类而非基类 {@code TankUnit}。 */
        public static final int ENTITY_ID = EntityMapping.register("endfield-industry-lib-tata-unit", TataUnit::new);

        /** 极大质量（普通单位质量约为 hitSize²·π，通常 ~200-3000），相对视为无穷大 → 不可被推动。 */
        private static final float HUGE_MASS = 4000f;

        @Override
        public int classId(){
            return ENTITY_ID;
        }

        @Override
        public void hitboxTile(Rect rect){
            // 地形碰撞箱与碰撞箱（hitSize）一致 → 完全不能走上地形/墙体
            rect.setCentered(x, y, hitSize, hitSize);
        }

        @Override
        public float mass(){
            return HUGE_MASS;
        }

        public static TataUnit create(){
            return new TataUnit();
        }
    }
}
