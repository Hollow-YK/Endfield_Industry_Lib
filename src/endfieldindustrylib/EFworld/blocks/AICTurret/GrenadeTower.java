package endfieldindustrylib.EFworld.blocks.AICTurret;

//榴弹塔
import arc.graphics.Color;
import endfieldindustrylib.EFcontents.EFitems;
import mindustry.content.Items;
import mindustry.entities.bullet.ArtilleryBulletType;
import mindustry.gen.Sounds;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.blocks.defense.turrets.ItemTurret;

public class GrenadeTower extends ItemTurret {
    public GrenadeTower(String name) {
        super(name);

        requirements(Category.turret, ItemStack.with(EFitems.origocrust, 15));

        alwaysUnlocked = true;
        consumePower(5f);

        size = 2;           // 方块占地 2×2 格
        range = 160f;       // 最大攻击范围（游戏单位，160 ≈ 20 格）
        reload = 180f;      // 装填间隔（180 ticks = 3 秒）
        rotateSpeed = 4f;   // 炮管旋转速度
        inaccuracy = 3f;    // 子弹散布角度
        maxAmmo = 20;       // 最大弹药存储量
        ammoPerShot = 1;    // 每次射击消耗 1 发弹药
        shootSound = Sounds.shootArtillery; // 炮击音效

        targetAir = false;  // 不对空（抛物线炮弹难以命中空中目标）
        targetGround = true; // 对地

        // 定义弹药类型映射：物品 → 对应的子弹属性
        ammo(
            // 弹药1：酮化灌木粉末（aketine-powder），小范围溅射
            EFitems.aketinePowder, new ArtilleryBulletType(8f, 0) {{ // 参数：子弹速度, 直接伤害（0=不造成直接伤害）
                splashDamage = 800f;        // 溅射伤害（对范围内所有敌人均摊）
                splashDamageRadius = 40f;   // 溅射半径（游戏单位，40 ≈ 2.5 格）
                trailColor = Color.valueOf("8ae86a");   // 弹道拖尾颜色（绿色）
                hitColor = Color.valueOf("8ae86a");     // 命中闪光颜色
                backColor = Color.valueOf("6bbd50");    // 子弹背面颜色（深绿）
                frontColor = Color.valueOf("a0f080");   // 子弹正面颜色（亮绿）

                width = 8f;     // 子弹精灵宽度
                height = 10f;   // 子弹精灵高度
                lifetime = 20f; // 存活时间（决定最大射程 = speed × lifetime / 8 格）
            }},
            // 弹药2：爆炸混合物（blast-compound，原版物品），大范围溅射
            EFitems.industrialExplosive, new ArtilleryBulletType(8f, 0) {{
                splashDamage = 1200f;       // 更高溅射伤害
                splashDamageRadius = 60f;   // 更大溅射半径（60 ≈ 3.75 格）
                trailColor = Color.valueOf("ff795e");   // 弹道颜色（橙色）
                hitColor = Color.valueOf("ff795e");
                backColor = Color.valueOf("e06040");    // 深橙
                frontColor = Color.valueOf("ffa080");   // 亮橙

                width = 10f;
                height = 12f;
                lifetime = 20f;
            }}
        );
    }
}
