package endfieldindustrylib.EFworld.blocks.AICTurret;

//电涌塔
import arc.graphics.Color;
import endfieldindustrylib.EFcontents.EFitems;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Sounds;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.blocks.defense.turrets.ItemTurret;

public class SurgeTower extends ItemTurret {
    public SurgeTower(String name) {
        super(name);

        requirements(Category.turret, ItemStack.with(EFitems.origocrust, 25));

        consumePower(20f);

        size = 2;
        range = 100f;       // 12.5m × 8 = 100
        reload = 180f;      // 3s
        rotateSpeed = 5f;
        inaccuracy = 2f;
        maxAmmo = 20;
        ammoPerShot = 1;
        shootSound = Sounds.shoot;

        targetAir = true;
        targetGround = true;

        ammo(
            EFitems.origocrust, new BasicBulletType(8f, 2443) {{ // half of 4886
                width = 8f;
                height = 12f;
                lifetime = 12.5f; // 100/8 = 12.5，确保 speed×lifetime=range
                keepVelocity = true;
                collides = true;
                collidesTiles = false;
                pierce = false;
                // TODO: chain lightning effect (bounce to nearby enemies, max 2 bounces)
            }},
            EFitems.amethystFiber, new BasicBulletType(8f, 4886) {{
                width = 8f;
                height = 12f;
                lifetime = 12.5f; // 100/8 = 12.5，确保 speed×lifetime=range
                keepVelocity = true;
                collides = true;
                collidesTiles = false;
                pierce = false;
                // TODO: chain lightning effect (bounce to nearby enemies, max 2 bounces)
            }}
        );
    }
}
