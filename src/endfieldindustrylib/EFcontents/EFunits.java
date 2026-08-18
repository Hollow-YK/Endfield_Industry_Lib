package endfieldindustrylib.EFcontents;

import endfieldindustrylib.EFworld.ai.FollowAI;
import endfieldindustrylib.EFworld.unit.Aggeloi.Heavyram;
import endfieldindustrylib.EFworld.unit.Aggeloi.Ram;
import endfieldindustrylib.EFworld.unit.Aggeloi.Sting;
import endfieldindustrylib.EFworld.unit.Landbreakers.Ambusher;
import endfieldindustrylib.EFworld.unit.Landbreakers.Infiltrator.InfiltratorType;
import endfieldindustrylib.EFworld.unit.Landbreakers.Raider;
import endfieldindustrylib.EFworld.unit.Landbreakers.Shield.VanguardShieldType;
import endfieldindustrylib.EFworld.unit.Landbreakers.Vanguard.VanguardType;
import endfieldindustrylib.EFworld.unit.Tata;
import mindustry.ai.UnitCommand;
import mindustry.gen.Unit;
import mindustry.type.UnitType;

/**
 * 塔卫二自定义单位。
 * <p>
 * 当前仅定义了一个单位——<b>塔塔</b>，即所有战役关卡中需要护送的单位。
 * 塔塔是地面单位，血量较高，移速极慢，无武器但有建造能力，
 * 且是唯一可以拆除"侵蚀核"的单位（侵蚀核的逻辑检查塔塔的类型）。
 */
public class EFunits {
    public static UnitType tata;
    public static UnitType ram;
    public static UnitType heavyram;
    public static UnitType sting;
    /** 伏击者（Landbreakers 人形族 T1 远程弩手） */
    public static UnitType ambusher;
    /** 突袭者（Landbreakers 人形族 T1 近战砍刀） */
    public static UnitType raider;
    /** 潜行者（Landbreakers 人形族 T1 高速刺客） */
    public static UnitType infiltrator;
    /** 先锋的盾牌（Landbreakers 人形族，由先锋动态生成并驱动） */
    public static UnitType vanguardShield;
    /** 先锋（Landbreakers 人形族 T1 盾戳单位） */
    public static UnitType vanguard;
    /** 自定义"跟随玩家"命令 */
    public static UnitCommand followCommand;

    public static void load() {
        // 创建自定义跟随命令
        followCommand = new UnitCommand("follow", "players", (Unit u) -> new FollowAI()) {{
            switchToMove = false;   // 不因右键点击而切换到移动
        }};

        // 实例化塔塔（具体定义见 EFworld.unit.Tata）
        tata = new Tata("tata");
        ram = new Ram("ram");
        // 实例化重装拉姆（Ram 的 T2 重装版，具体定义见 EFworld.unit.Heavyram）
        heavyram = new Heavyram("heavyram");
        // 实例化刺蝎（蝎形四足远程单位，具体定义见 EFworld.unit.Sting）
        sting = new Sting("sting");
        // 实例化伏击者（人形弩手，具体定义见 EFworld.unit.Landbreakers.Ambusher）
        ambusher = new Ambusher("ambusher");
        // 实例化突袭者（人形砍刀近战，具体定义见 EFworld.unit.Landbreakers.Raider）
        raider = new Raider("raider");
        // 实例化潜行者（人形高速刺客，实体定义见 EFworld.unit.Landbreakers.Infiltrator）
        infiltrator = new InfiltratorType("infiltrator");
        // 盾牌须先于先锋实例化：先锋单位生成时会动态调用 vanguardShield.spawn 创建盾牌
        vanguardShield = new VanguardShieldType("vanguard-shield");
        // 实例化先锋（人形盾戳单位，实体见 EFworld.unit.Landbreakers.Vanguard，类型见 VanguardType）
        vanguard = new VanguardType("vanguard");
    }
}

