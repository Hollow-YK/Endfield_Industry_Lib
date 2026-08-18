package endfieldindustrylib.EFcontents;

import mindustry.type.SectorPreset;

/**
 * 塔卫二战役关卡预设。
 * <p>
 * 每个关卡对应一个扇区，使用默认的 FileMapGenerator 加载地图文件。
 * 后续需要将对应的 .msav 地图文件放入 {@code assets/maps/} 目录下，
 * 文件名需与 SectorPreset 名称一致（例如 {@code theHub.msav}）。
 * <p>
 * 扇区坐标（构造参数中的数字）为行星网格上的位置索引，
 * 需与后续制作的 .msav 地图文件中保存的坐标一致。
 */
public class EFsectorPresets {
    public static SectorPreset
        theHub,
        theHubII,
        originiumSciencePark,
        originLodespring,
        powerPlateau,
        originiumScienceParkII,
        originLodespringII,
        powerPlateauII;

    public static void load() {
        // ===================================================================
        //  枢纽区（新手教程）
        // ===================================================================
        // 坐标 0 — 需地图文件: assets/maps/theHub.msav
        theHub = new SectorPreset("theHub", EFplanets.taelosII, 0) {{
            alwaysUnlocked = true;
            addStartingItems = true;
            startWaveTimeMultiplier = 3f;
            captureWave = 5;
            difficulty = 1;
            rules = r -> {
                r.winWave = captureWave;
            };
        }};
        // 坐标 1 — 需地图文件: assets/maps/theHub-ii.msav
        theHubII = new SectorPreset("theHub-ii", EFplanets.taelosII, 1) {{
            addStartingItems = true;
            startWaveTimeMultiplier = 3f;
            noLighting = true;
            difficulty = 3;
            rules = r -> {
                r.winWave = 0;
            };
            shieldSectors.add(EFsectorPresets.theHub.sector);
        }};

        // ===================================================================
        //  源石研究所
        // ===================================================================
        originiumSciencePark = new SectorPreset("originiumSciencePark", EFplanets.taelosII, 15) {{
            captureWave = 30;
            noLighting = true;
            difficulty = 5;
            rules = r -> {
                r.winWave = captureWave;
            };
            shieldSectors.add(EFsectorPresets.theHub.sector);
        }};
        originiumScienceParkII = new SectorPreset("originiumSciencePark-ii", EFplanets.taelosII, 16) {{
            difficulty = 5;
            noLighting = true;
            rules = r -> {
                r.winWave = 0;
            };
            shieldSectors.add(EFsectorPresets.originiumSciencePark.sector);
        }};

        // ===================================================================
        //  矿脉园区
        // ===================================================================
        // 坐标 30 — 需地图文件: assets/maps/originLodespring.msav
        originLodespring = new SectorPreset("originLodespring", EFplanets.taelosII, 30) {{
            difficulty = 7;
            noLighting = true;
            rules = r -> {
                r.winWave = 0;
            };
            shieldSectors.add(EFsectorPresets.originiumSciencePark.sector);
        }};
        // 坐标 31 — 需地图文件: assets/maps/originLodespring-ii.msav
        originLodespringII = new SectorPreset("originLodespring-ii", EFplanets.taelosII, 31) {{
            difficulty = 7;
            noLighting = true;
            rules = r -> {
                r.winWave = 0;
            };
            shieldSectors.add(EFsectorPresets.originLodespring.sector);
        }};

        // ===================================================================
        //  供能高地（最终战役）
        // ===================================================================
        // 坐标 45 — 需地图文件: assets/maps/powerPlateau.msav
        powerPlateau = new SectorPreset("powerPlateau", EFplanets.taelosII, 45) {{
            difficulty = 9;
            noLighting = true;
            rules = r -> {
                r.winWave = 0;
            };
            shieldSectors.add(EFsectorPresets.originLodespring.sector);
        }};
        // 坐标 46 — 需地图文件: assets/maps/powerPlateau-ii.msav
        powerPlateauII = new SectorPreset("powerPlateau-ii", EFplanets.taelosII, 46) {{
            difficulty = 9;
            isLastSector = true;
            noLighting = true;
            rules = r -> {
                r.winWave = 0;
            };
            shieldSectors.add(EFsectorPresets.powerPlateau.sector);
        }};
    }
}
