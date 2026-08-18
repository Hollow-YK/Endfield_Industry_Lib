package endfieldindustrylib.EFworld.blocks.AICTurret;

//哨戒塔
import arc.graphics.Color;
import endfieldindustrylib.EFcontents.EFitems;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Sounds;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.blocks.defense.turrets.ItemTurret;

public class SentryTower extends ItemTurret {
    public SentryTower(String name) {
        super(name);

        requirements(Category.turret, ItemStack.with(EFitems.origocrust, 25));

        consumePower(20f);

        size = 2;
        range = 160f;       // 20m × 8 = 160
        reload = 300f;      // 5s
        rotateSpeed = 4f;
        inaccuracy = 1f;
        maxAmmo = 20;
        ammoPerShot = 1;
        shootSound = Sounds.shoot;

        targetAir = true;
        targetGround = true;

        ammo(
            EFitems.origocrust, new BasicBulletType(8f, 5817) {{ // half of 11634
                width = 10f;
                height = 18f;
                lifetime = 20f; // 160/8 = 20
                keepVelocity = true;
                collides = true;
                collidesTiles = false;
                pierce = false;
                // TODO: prioritize high-threat targets
            }},
            EFitems.amethystFiber, new BasicBulletType(8f, 11634) {{
                width = 10f;
                height = 18f;
                lifetime = 20f; // 160/8 = 20
                keepVelocity = true;
                collides = true;
                collidesTiles = false;
                pierce = false;
                // TODO: prioritize high-threat targets
            }}
        );
    }
}
