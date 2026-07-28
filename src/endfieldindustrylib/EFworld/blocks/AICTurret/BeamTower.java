package endfieldindustrylib.EFworld.blocks.AICTurret;

//射线塔
import arc.graphics.Color;
import endfieldindustrylib.EFcontents.EFitems;
import mindustry.content.Fx;
import mindustry.entities.bullet.SapBulletType;
import mindustry.entities.pattern.ShootPattern;
import mindustry.gen.Sounds;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.blocks.defense.turrets.PowerTurret;

public class BeamTower extends PowerTurret {

    public BeamTower(String name) {
        super(name);

        requirements(Category.turret, ItemStack.with(EFitems.origocrust, 25));
        consumePower(20f);

        size = 2;
        range = 100f;       // 12.5m × 8 = 100
        reload = 480f;      // 8s
        rotateSpeed = 10f;
        shootCone = 360f;   // 全向，自瞄保证方向
        shootSound = Sounds.shootLaser;

        targetAir = true;
        targetGround = true;

        // 射击模式：首发延迟（给自瞄留出旋转时间）
        shoot = new ShootPattern();
        shoot.firstShotDelay = 5f;  // 5 ticks ≈ 0.08s 延迟，足够自瞄旋转
        shoot.shotDelay = 0f;

        // 自瞄 Building 类：开火前强制对准目标
        buildType = BeamTowerBuild::new;

        // SapBulletType：从炮塔到目标绘制一条激光束，仅命中第一个目标
        shootType = new SapBulletType() {{
            damage = 22338;
            length = 100f;           // 与 range 一致（12.5格）
            lengthRand = 0f;
            sapStrength = 0f;        // 不吸血
            color = Color.valueOf("ff4444");
            width = 0.8f;
            lifetime = 10f;          // 光束短暂停留以可见
            status = null;
            hitEffect = Fx.hitLancer;
            shootEffect = Fx.lightningShoot;
            smokeEffect = Fx.none;
            despawnEffect = Fx.none;
            lightColor = Color.valueOf("ff4444");
            lightOpacity = 0.5f;
        }};

        shootX = 0f;
        shootY = 0f;
    }

    public class BeamTowerBuild extends PowerTurretBuild {
        @Override
        public void updateTile() {
            // 提前索敌，确保 target 在基类开枪前就已存在
            if(timer(timerTarget, target != null ? newTargetInterval : targetInterval)){
                findTarget();
            }
            if(!validateTarget()) target = null;

            // 自瞄：强制对准目标
            if(target != null){
                rotation = angleTo(target);
            }
            super.updateTile();
        }
    }
}
