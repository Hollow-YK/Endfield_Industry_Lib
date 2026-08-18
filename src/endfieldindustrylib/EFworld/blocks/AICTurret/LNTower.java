package endfieldindustrylib.EFworld.blocks.AICTurret;

//液氮塔
import arc.graphics.Color;
import endfieldindustrylib.EFcontents.EFitems;
import endfieldindustrylib.EFcontents.EFstatusEffects;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Sounds;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.blocks.defense.turrets.ItemTurret;

public class LNTower extends ItemTurret {
    public LNTower(String name) {
        super(name);

        requirements(Category.turret, ItemStack.with(EFitems.origocrust, 15));

        consumePower(20f);

        size = 2;
        range = 120f;       // 15m × 8 = 120（1m=1格=8单位）
        reload = 300f;      // 5s
        rotateSpeed = 4f;
        inaccuracy = 0f;
        maxAmmo = 20;
        ammoPerShot = 1;
        shootSound = Sounds.shoot;

        targetAir = false;
        targetGround = true;

        ammo(
            EFitems.origocrust, new BasicBulletType(8f, 0) {{ // 速度改为 1格/帧（8f）
                splashDamage = 0f;
                splashDamageRadius = 40f; // radius 5 tiles，范围内敌方均承受液氮效果
                status = EFstatusEffects.lnTowerEffect; // 液氮：移动速度下降75%
                statusDuration = 180f; // 3秒（60ticks/秒）
                width = 9f;
                height = 14f;
                lifetime = 15f; // 120/8 = 15，与 range 匹配
                keepVelocity = true;
                collides = true;
                collidesTiles = false;
                pierce = false;
            }},
            EFitems.amethystFiber, new BasicBulletType(8f, 0) {{ // 速度改为 1格/帧（8f）
                splashDamage = 0f;
                splashDamageRadius = 40f; // radius 5 tiles，范围内敌方均承受液氮效果
                status = EFstatusEffects.lnTowerEffect; // 液氮：移动速度下降75%
                statusDuration = 180f; // 3秒（60ticks/秒）
                width = 9f;
                height = 14f;
                lifetime = 40f;
                keepVelocity = true;
                collides = true;
                collidesTiles = false;
                pierce = false;
            }}
        );
    }
}
