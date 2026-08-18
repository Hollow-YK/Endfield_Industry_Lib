package endfieldindustrylib.EFworld.blocks.AICTurret;

//扩装铳械塔
import arc.graphics.Color;
import endfieldindustrylib.EFcontents.EFitems;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Sounds;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.blocks.defense.turrets.ItemTurret;

public class HeavyGunTower extends ItemTurret {
    public HeavyGunTower(String name) {
        super(name);

        requirements(Category.turret, ItemStack.with(EFitems.origocrust, 20));

        consumePower(20f);

        size = 2;
        range = 120f;       // 15m × 8 = 120
        reload = 120f;      // 2s
        rotateSpeed = 5f;
        inaccuracy = 2f;
        maxAmmo = 30;
        ammoPerShot = 1;
        shootSound = Sounds.shoot;

        targetAir = true;
        targetGround = true;

        ammo(
            EFitems.origocrust, new BasicBulletType(8f, 1068) {{ // 速度改为 1格/帧（8f），half of 2136
                width = 10f;
                height = 16f;
                lifetime = 15f; // 120/8 = 15
                keepVelocity = true;
                collides = true;
                collidesTiles = false;
                pierce = false;
            }},
            EFitems.amethystFiber, new BasicBulletType(8f, 2136) {{ // 速度改为 1格/帧（8f）
                width = 10f;
                height = 16f;
                lifetime = 15f; // 120/8 = 15
                keepVelocity = true;
                collides = true;
                collidesTiles = false;
                pierce = false;
            }}
        );
    }
}
