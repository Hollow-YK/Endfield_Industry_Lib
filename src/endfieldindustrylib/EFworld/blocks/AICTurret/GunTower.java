package endfieldindustrylib.EFworld.blocks.AICTurret;

//铳械塔
import endfieldindustrylib.EFcontents.EFitems;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.pattern.ShootPattern;
import mindustry.gen.Sounds;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.blocks.defense.turrets.ItemTurret;

public class GunTower extends ItemTurret {
    public GunTower(String name) {
        super(name);

        requirements(Category.turret, ItemStack.with(EFitems.origocrust, 10));

        alwaysUnlocked = true;
        size = 2;
        range = 120f;       // 最大攻击/索敌范围（游戏单位，8 ≈ 1 格）
        reload = 120f;      // 装填间隔（游戏帧数，120 ticks = 2 秒，60 ticks/秒）
        rotateSpeed = 16f;   // 炮管旋转速度（度/帧），越大转向越快
        inaccuracy = 0f;    // 子弹散布角度（度），0 为精确瞄准
        maxAmmo = 30;       // 最大弹药存储量，装填满后停止消耗
        ammoPerShot = 1;    // 每次射击消耗 1 发弹药
        shootSound = Sounds.shoot; // 射击音效
        shoot = new ShootPattern() {{ // 射击模式：三连发
            shots = 3;        // 每次开火打出 3 发子弹
            shotDelay = 2f;   // 每发间隔 2 帧（≈0.033秒）
        }}; // consumeAmmoOnce 默认为 true，因此消耗 1 发弹药完成全部 3 发射击

        // 定义弹药类型映射：物品 → 对应的子弹属性
        ammo(
            // 弹药1：晶体外壳（origocrust），低伤害
            EFitems.origocrust, new BasicBulletType(8f, 609) {{ // 参数：子弹速度(格/秒×8), 基础伤害
                width = 9f;         // 子弹精灵宽度（像素）
                height = 14f;       // 子弹精灵高度（像素）
                lifetime = 15f;     // 子弹存活时间（帧），120/8 = 15，与 range 匹配
                keepVelocity = true; // 继承炮台移动速度（炮台通常是静态，保持 true 即可）
                collides = true;    // 是否与单位碰撞
                collidesTiles = false; // 不与地形方块碰撞（子弹穿过建筑）
                pierce = false;     // 不穿透敌人，命中第一个即消失
            }},
            // 弹药2：紫晶纤维（amethyst-fiber），高伤害
            EFitems.amethystFiber, new BasicBulletType(8f, 761.25f) {{
                width = 9f;
                height = 14f;
                lifetime = 15f; // 120/8 = 15
                keepVelocity = true;
                collides = true;
                collidesTiles = false;
                pierce = false;
            }}
        );
    }
}
