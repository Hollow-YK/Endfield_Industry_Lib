package endfieldindustrylib.EFworld.blocks.AICTurret;

//全向声波塔 — 以自身为中心周期释放晕眩脉冲，使用虚拟子弹触发 AoE
import arc.graphics.Color;
import endfieldindustrylib.EFcontents.EFitems;
import endfieldindustrylib.EFcontents.EFstatusEffects;
import mindustry.content.Fx;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.effect.WaveEffect;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.blocks.defense.turrets.PowerTurret;

public class OmnidirectionalSonicTower extends PowerTurret {

    public OmnidirectionalSonicTower(String name) {
        super(name);

        requirements(Category.turret, ItemStack.with(EFitems.origocrust, 20));
        consumePower(20f);

        size = 2;
        range = 48f;        // 6m × 8 = 48
        reload = 300f;      // 5s
        rotateSpeed = 4f;
        shootCone = 360f;   // 全向，无需精确对准

        targetAir = true;
        targetGround = true;

        // 使用自定义 Building 类（用于首次装填初始化）
        buildType = OmnidirectionalSonicTowerBuild::new;

        // 虚拟子弹：不飞行、不可见、仅在生成点立即触发溅射状态
        shootType = new BasicBulletType(0f, 0f) {{
            lifetime = 1f;                // 1 tick 后消失
            despawnHit = true;            // 消失时触发 hit（→ createSplashDamage）
            keepVelocity = false;
            hittable = false;
            collides = false;

            // 关闭默认射击特效，使用自定义扩散波
            shootEffect = new WaveEffect() {{
                colorFrom = Color.valueOf("ffff44");
                colorTo = Color.valueOf("ffffff");
                sizeFrom = 0f;
                sizeTo = 48f;
                strokeFrom = 3f;
                strokeTo = 0f;
                sides = 24;
                lifetime = 20f;
            }};
            smokeEffect = Fx.none;
            hitEffect = Fx.none;
            despawnEffect = Fx.none;

            splashDamage = 0f;
            splashDamageRadius = 48f;     // 6 格半径范围内施加状态
            status = EFstatusEffects.omnidirectionalSonicTowerEffect;
            statusDuration = 180f;        // 3 秒
        }};

        // 子弹生成在塔中心
        shootX = 0f;
        shootY = 0f;
    }

    public class OmnidirectionalSonicTowerBuild extends PowerTurretBuild {
        @Override
        public void updateTile() {
            super.updateTile();

            // 无目标时保持装填满，敌方进入范围后立即射击
            if(target == null) {
                reloadCounter = reload;
            }
        }
    }
}
