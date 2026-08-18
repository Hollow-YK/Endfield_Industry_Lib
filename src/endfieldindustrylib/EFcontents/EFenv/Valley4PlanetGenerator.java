package endfieldindustrylib.EFcontents.EFenv;

import static java.lang.Math.random;
import java.util.Random;

import arc.graphics.Color;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec3;
import arc.util.noise.Simplex;
import mindustry.content.Blocks;
import mindustry.content.Loadouts;
import mindustry.game.Schematics;
import mindustry.maps.generators.PlanetGenerator;
import mindustry.type.Sector;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.TileGen;
import mindustry.world.blocks.environment.Floor;

/** 四号谷地枢纽区 — 群系系统版 */
public class Valley4PlanetGenerator extends PlanetGenerator {

    // ==================== 群系定义（内部类）====================

    /** 单个群系定义 */
    public static class Biome {
        public final String name;
        public final float areaScale;            // 面积大小（越小斑块越大）
        public final float heightAmplitude;      // 地形起伏高度
        public final float heightFrequency;      // 地形起伏频率
        public final String group;               // 关联群系组
        public final int priority;               // 生成优先级（高值覆盖低值）
        public final Block[] floor;              // 地面方块（低→高）
        public final float presence;             // 出现阈值 [0,1]

        public Biome(String name, float areaScale, float heightAmplitude,
                     float heightFrequency, String group, int priority,
                     Block[] floor, float presence) {
            this.name = name;
            this.areaScale = areaScale;
            this.heightAmplitude = heightAmplitude;
            this.heightFrequency = heightFrequency;
            this.group = group;
            this.priority = priority;
            this.floor = floor;
            this.presence = presence;
        }

        public Block getFloor(float h) {
            int idx = Mathf.clamp((int)(h * floor.length), 0, floor.length - 1);
            return floor[idx];
        }

        @Override
        public String toString() { return name; }
    }

    // ==================== 自定义 3D 噪声 ====================

    public static class CustomNoise {
        private final int[] perm;
        public CustomNoise(int seed) {
            Random rng = new Random(seed);
            int[] p = new int[256];
            for (int i = 0; i < 256; i++) p[i] = i;
            for (int i = 255; i > 0; i--) {
                int j = rng.nextInt(i + 1);
                int tmp = p[i]; p[i] = p[j]; p[j] = tmp;
            }
            perm = new int[512];
            for (int i = 0; i < 512; i++) perm[i] = p[i & 255];
        }

        private int hash3(int x, int y, int z) {
            return perm[perm[perm[x & 255] + (y & 255)] + (z & 255)] & 255;
        }

        private float smoothstep(float t) { return t * t * (3f - 2f * t); }
        private float lerp(float a, float b, float t) { return a + t * (b - a); }

        public float noise3d(float x, float y, float z) {
            int ix = (int)Math.floor(x), iy = (int)Math.floor(y), iz = (int)Math.floor(z);
            float fx = x - ix, fy = y - iy, fz = z - iz;
            float sx = smoothstep(fx), sy = smoothstep(fy), sz = smoothstep(fz);

            float n000 = (hash3(ix,  iy,  iz)   / 127.5f) - 1f;
            float n100 = (hash3(ix+1, iy,  iz)   / 127.5f) - 1f;
            float n010 = (hash3(ix,  iy+1, iz)   / 127.5f) - 1f;
            float n110 = (hash3(ix+1, iy+1, iz)   / 127.5f) - 1f;
            float n001 = (hash3(ix,  iy,  iz+1) / 127.5f) - 1f;
            float n101 = (hash3(ix+1, iy,  iz+1) / 127.5f) - 1f;
            float n011 = (hash3(ix,  iy+1, iz+1) / 127.5f) - 1f;
            float n111 = (hash3(ix+1, iy+1, iz+1) / 127.5f) - 1f;

            float nx0 = lerp(lerp(n000, n100, sx), lerp(n010, n110, sx), sy);
            float nx1 = lerp(lerp(n001, n101, sx), lerp(n011, n111, sx), sy);
            return lerp(nx0, nx1, sz);
        }

        public float fbm(float x, float y, float z, int octaves, float persistence, float lacunarity) {
            float value = 0f, amplitude = 1f, maxValue = 0f, frequency = 1f;
            for (int i = 0; i < octaves; i++) {
                value += noise3d(x * frequency, y * frequency, z * frequency) * amplitude;
                maxValue += amplitude;
                amplitude *= persistence;
                frequency *= lacunarity;
            }
            return value / maxValue;
        }
    }

    // ==================== 群系系统 ====================

    public static class BiomeSystem {
        public final Biome[] biomes;
        public final CustomNoise noise;
        public final int width, height;
        public final Biome[][] biomeMap;
        public final float[][] heightMap;

        public BiomeSystem(Biome[] biomes, int width, int height, int seed) {
            this.biomes = biomes;
            this.width = width;
            this.height = height;
            this.noise = new CustomNoise(seed);
            this.biomeMap = new Biome[height][width];
            this.heightMap = new float[height][width];
        }

        /** 生成群系地图，只生成指定的群系（null=全部） */
        public void generateMap(Biome... onlyThese) {
            Biome[] active = (onlyThese == null || onlyThese.length == 0) ? biomes : onlyThese;
            Biome fallback = findLowestPriority(active);

            // 第一步：计算每个格子的群系
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    float wx = x * 0.3f, wy = y * 0.3f;
                    Biome best = null;
                    float bestScore = -999f;

                    for (Biome b : active) {
                        float suitability = noise.fbm(
                            wx / b.areaScale, wy / b.areaScale, 0f,
                            3, 0.5f, 2f
                        );
                        if (suitability < b.presence) continue;
                        float score = suitability + b.priority * 0.1f;
                        if (score > bestScore) { bestScore = score; best = b; }
                    }
                    biomeMap[y][x] = (best != null) ? best : fallback;
                }
            }

            // 第二步：平滑过滤孤立散点
            for (int pass = 0; pass < 3; pass++) {
                Biome[][] smoothed = new Biome[height][width];
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        java.util.HashMap<Biome, Integer> cnt = new java.util.HashMap<>();
                        int total = 0;
                        for (int dy = -2; dy <= 2; dy++) {
                            for (int dx = -2; dx <= 2; dx++) {
                                int nx = x + dx, ny = y + dy;
                                if (nx < 0 || nx >= width || ny < 0 || ny >= height) continue;
                                cnt.put(biomeMap[ny][nx], cnt.getOrDefault(biomeMap[ny][nx], 0) + 1);
                                total++;
                            }
                        }
                        Biome best = biomeMap[y][x];
                        int bestC = 0;
                        for (java.util.Map.Entry<Biome, Integer> e : cnt.entrySet()) {
                            if (e.getValue() > bestC) { bestC = e.getValue(); best = e.getKey(); }
                        }
                        if (best != biomeMap[y][x]) {
                            smoothed[y][x] = best;
                        } else {
                            smoothed[y][x] = biomeMap[y][x];
                        }
                    }
                }
                for (int y = 0; y < height; y++) {
                    System.arraycopy(smoothed[y], 0, biomeMap[y], 0, width);
                }
            }

            // 第三步：生成每个群系内部的地形高度
            generateHeights();
        }

        private void generateHeights() {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Biome b = biomeMap[y][x];
                    float wx = x * 0.3f, wy = y * 0.3f;

                    float raw = noise.fbm(
                        wx * b.heightFrequency, wy * b.heightFrequency, 0f,
                        5, 0.6f, 2f
                    );

                    float h = (raw + 1f) / 2f * b.heightAmplitude;
                    heightMap[y][x] = Mathf.clamp(h, 0f, 1f);
                }
            }
        }

        public Block getFloorAt(int x, int y) {
            if (x < 0 || x >= width || y < 0 || y >= height) return Blocks.air;
            return biomeMap[y][x].getFloor(heightMap[y][x]);
        }

        public Biome getBiomeAt(int x, int y) {
            if (x < 0 || x >= width || y < 0 || y >= height) return null;
            return biomeMap[y][x];
        }

        private Biome findLowestPriority(Biome[] arr) {
            Biome best = arr[0];
            for (Biome b : arr) {
                if (b.priority < best.priority) best = b;
            }
            return best;
        }
    }

    // ==================== 预设群系 ====================

    private static Biome[] createDefaultBiomes() {
        return new Biome[] {
            new Biome("平原",    16f, 0.15f, 0.3f, "flat", 0,
                new Block[]{Blocks.grass, Blocks.grass, Blocks.grass, Blocks.grass, Blocks.stone}, 0f),
            new Biome("稀疏平原", 12f, 0.2f,  0.4f, "flat", 1,
                new Block[]{Blocks.sand, Blocks.grass, Blocks.grass, Blocks.grass, Blocks.stone}, 0f),
            new Biome("沙地",     8f,  0.1f,  0.5f, "flat", 2,
                new Block[]{Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.stone}, 0.25f),
            new Biome("荒地",    10f,  0.3f,  0.6f, "rough", 3,
                new Block[]{Blocks.stone, Blocks.stone, Blocks.stone, Blocks.stone, Blocks.stone}, 0.15f),
            new Biome("山谷",     6f,  0.4f,  1.0f, "valley", 4,
                new Block[]{Blocks.sand, Blocks.grass, Blocks.stone, Blocks.stone, Blocks.stone}, 0.35f),
            new Biome("山地",    10f,  0.8f,  0.8f, "high", 5,
                new Block[]{Blocks.stone, Blocks.stone, Blocks.stone, Blocks.stone, Blocks.stone}, 0f),
            new Biome("河滩",     4f,  0.05f, 0.2f, "water", 8,
                new Block[]{Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand}, 0.45f),
            new Biome("河流",     4f,  0f,    0f,   "water", 9,
                new Block[]{Blocks.water, Blocks.water, Blocks.water, Blocks.water, Blocks.water}, 0.80f),
        };
    }

    // ==================== 生成器字段 ====================

    float heightMult = 1.0f;
    private BiomeSystem biomeSystem;

    {
        baseSeed = (int) (random() * 999999);
        defaultLoadout = Loadouts.basicShard;
    }

    @Override
    public float getHeight(Vec3 position) {
        return (rawHeight(position) + 1f) / 2f * heightMult;
    }

    @Override
    public void getColor(Vec3 position, Color out) {
        float h = getHeight(position);
        // 纬度因子：0=赤道，1=两极
        float latFactor = Math.abs(position.y);

        // 添加小规模细节噪声让颜色更自然
        float detailNoise = Simplex.noise3d(seed + 3, 3, 0.5f, 0.8f,
            position.x * 2f, position.y * 2f, position.z * 2f) * 0.06f;

        if (h < 0.42f + detailNoise * 0.5f) {
            // —— 深海（深蓝）——
            float depth = h / 0.42f;
            out.set(Color.valueOf("0a2a5c")).lerp(Color.valueOf("1a4a7a"), depth);
        } else if (h < 0.48f + detailNoise * 0.5f) {
            // —— 浅海/近岸（青蓝）——
            float t = (h - 0.42f) / 0.06f;
            out.set(Color.valueOf("1a5a8a")).lerp(Color.valueOf("3a8ab8"), t);
        } else if (h < 0.52f + detailNoise) {
            // —— 海滩/沙岸（沙黄色）——
            out.set(Color.valueOf("c8b480"));
        } else if (h < 0.72f + detailNoise * 0.3f) {
            // —— 低地/平原（绿色，随纬度变化）——
            float t = (h - 0.52f) / 0.20f;
            Color green;
            if (latFactor > 0.65f) {
                // 寒带：深绿/针叶林
                green = Color.valueOf("3a6a2a");
            } else if (latFactor > 0.35f) {
                // 温带：翠绿
                green = Color.valueOf("5cb85c");
            } else {
                // 热带：茂盛鲜绿
                green = Color.valueOf("6aaa3a");
            }
            // 随高度从绿渐变到黄褐
            out.set(green).lerp(Color.valueOf("9a8a4a"), t * 0.4f);
        } else if (h < 0.85f + detailNoise * 0.3f) {
            // —— 高地/丘陵（黄褐→棕）——
            float t = (h - 0.72f) / 0.13f;
            out.set(Color.valueOf("8a7a4a")).lerp(Color.valueOf("7a6a4a"), t);
        } else if (h < 0.94f) {
            // —— 山脉（棕灰）——
            out.set(Color.valueOf("8a8a7a")).lerp(Color.valueOf("9a9a8a"),
                (h - 0.85f) / 0.09f);
        } else {
            // —— 雪顶（白色，两极更常见）——
            float snowThreshold = latFactor > 0.5f ? 0.88f : 0.94f;
            if (h > snowThreshold) {
                out.set(Color.valueOf("e8e8f0"));
            } else {
                out.set(Color.valueOf("9a9a8a"));
            }
        }

        // 微弱的极地冰盖叠加（高纬度地区泛白）
        if (latFactor > 0.7f && h < 0.5f) {
            float iceBlend = (latFactor - 0.7f) / 0.3f * 0.5f;
            out.lerp(Color.valueOf("c8d8e8"), iceBlend);
        }
    }

    @Override
    public float getSizeScl() {
        return 5000f;
    }

    /**地图尺寸为 800×800（最大尺寸） */
    @Override
    public int getSectorSize(Sector sector) {
        return 600;
    }

    /** 覆写为空方法，去掉游戏源码 trimDark() 导致的错误黑色边界 */
    @Override
    public void trimDark() {
        // 不做任何操作，避免产生黑色边缘墙壁
    }

    float rawHeight(Vec3 position) {
        float main = Simplex.noise3d(seed, 7, 0.6f, 1f / 0.3f, position.x, position.y, position.z);
        float detail = Simplex.noise3d(seed + 1, 4, 0.5f, 1f / 0.08f, position.x + 100f, position.y, position.z + 50f);
        return main * 0.75f + detail * 0.25f;
    }

    @Override
    public void genTile(Vec3 position, TileGen tile) {
        tile.floor = Blocks.grass;
        tile.block = Blocks.air;
    }

    int[] findSpawn(int cx, int cy, int range) {
        for (int r = 0; r <= range; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    int x = cx + dx, y = cy + dy;
                    if (!tiles.in(x, y)) continue;
                    Tile t = tiles.getn(x, y);
                    if (t.block() == Blocks.air && !t.floor().isLiquid) {
                        return new int[]{x, y};
                    }
                }
            }
        }
        return new int[]{cx, cy};
    }

    Block floorAt(int x, int y) {
        return tiles.in(x, y) ? tiles.getn(x, y).floor() : Blocks.air;
    }

    /** ===== 群系生态细节装饰系统 ===== */
    private void decorateBiomes() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Tile tile = tiles.getn(x, y);
                if (tile.block() != Blocks.air) continue;
                Biome biome = biomeSystem.getBiomeAt(x, y);
                if (biome == null) continue;

                switch (biome.name) {
                    case "山地": {
                        if (rand.chance(0.015)) {
                            tile.setBlock(Blocks.stoneWall);
                        } else if (rand.chance(0.04)) {
                            tile.setBlock(Blocks.boulder);
                        } else if (rand.chance(0.015)) {
                            // TODO: 沙叶（sand leaf）— 只在山地生长
                            tile.setBlock(Blocks.shrubs);
                        }
                        break;
                    }
                    case "山谷": {
                        if (rand.chance(0.015)) {
                            tile.setBlock(Blocks.stoneWall);
                        } else if (rand.chance(0.03)) {
                            tile.setBlock(Blocks.boulder);
                        }
                        break;
                    }
                    case "平原": {
                        if (rand.chance(0.005)) {
                            tile.setBlock(Blocks.boulder);
                        }
                        break;
                    }
                    case "稀疏平原": {
                        if (rand.chance(0.008)) {
                            tile.setBlock(Blocks.boulder);
                        }
                        break;
                    }
                    case "沙地": {
                        break;
                    }
                    case "荒地": {
                        if (rand.chance(0.01)) {
                            tile.setBlock(Blocks.boulder);
                        }
                        break;
                    }
                    case "河滩": {
                        if (rand.chance(0.003)) {
                            tile.setBlock(Blocks.boulder);
                        }
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void generate() {
        int cx = width / 2, cy = height / 2;
        int spawnX = cx + rand.random(-width / 6, width / 6);
        int spawnY = cy + rand.random(-height / 6, height / 6);

        // ===== 第一步：初始化群系系统 =====
        Biome[] allBiomes = createDefaultBiomes();
        biomeSystem = new BiomeSystem(allBiomes, width, height, seed + 100);
        biomeSystem.generateMap();

        // ===== 第二步：铺设地形 =====
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Tile tile = tiles.getn(x, y);
                tile.setFloor((Floor) biomeSystem.getFloorAt(x, y));
            }
        }

        // ===== 河流（概率路径生成，仅一条蜿蜒河流）=====
        boolean hasRiver = rand.chance(0.55f);
        if (hasRiver) {
            CustomNoise riverPathNoise = new CustomNoise(seed + 200);
            int maxSteps = (int)(Math.max(width, height) * 1.8f);

            // 随机选择起始边和起始点
            int edge = rand.random(3);
            float sx, sy;
            float startAngle;
            switch (edge) {
                case 0: sx = rand.random(width); sy = 0; startAngle = 90f; break;
                case 1: sx = rand.random(width); sy = height - 1; startAngle = -90f; break;
                case 2: sx = 0; sy = rand.random(height); startAngle = 0f; break;
                default: sx = width - 1; sy = rand.random(height); startAngle = 180f; break;
            }

            boolean crossesMap = rand.chance(0.5f);

            float px = sx, py = sy;
            float angle = startAngle + (rand.nextFloat() - 0.5f) * 60f;
            boolean[][] riverPath = new boolean[height][width];
            int steps = 0;

            for (int i = 0; i < maxSteps; i++) {
                int ix = Mathf.clamp(Math.round(px), 0, width - 1);
                int iy = Mathf.clamp(Math.round(py), 0, height - 1);
                riverPath[iy][ix] = true;

                float noiseVal = riverPathNoise.fbm(px * 0.06f, py * 0.06f, 0f, 3, 0.5f, 2f);
                angle += noiseVal * 40f;

                if (crossesMap) {
                    boolean reached = false;
                    switch (edge) {
                        case 0: if (py >= height - 1) reached = true; break;
                        case 1: if (py <= 0) reached = true; break;
                        case 2: if (px >= width - 1) reached = true; break;
                        case 3: if (px <= 0) reached = true; break;
                    }
                    if (reached) break;
                } else {
                    steps++;
                    if (rand.chance(steps / (float)maxSteps * 0.35f)) break;
                }

                px += Mathf.cos(angle);
                py += Mathf.sin(angle);
            }

            // 应用河流
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (riverPath[y][x]) {
                        tiles.getn(x, y).setFloor((Floor) Blocks.water);
                        tiles.getn(x, y).setBlock(Blocks.air);
                    } else {
                        boolean nearRiver = false;
                        for (int dy = -1; dy <= 1 && !nearRiver; dy++) {
                            for (int dx = -1; dx <= 1 && !nearRiver; dx++) {
                                int nx = x + dx, ny = y + dy;
                                if (nx >= 0 && nx < width && ny >= 0 && ny < height && riverPath[ny][nx]) {
                                    nearRiver = true;
                                }
                            }
                        }
                        if (nearRiver && tiles.getn(x, y).floor() != Blocks.water) {
                            tiles.getn(x, y).setFloor((Floor) Blocks.sand);
                        }
                    }
                }
            }
        }

        cells(5);

        // ===== 连续墙体（只限山地/山谷/荒地 — 放宽条件版）=====
        CustomNoise wallNoise = new CustomNoise(seed + 500);
        boolean[][] wallSeed = new boolean[width][height];
        java.util.Random scatterRng = new java.util.Random(seed + 800);
        // 第一步：大量标记种子点
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (floorAt(x, y) == Blocks.stone) {
                    Biome b = biomeSystem.getBiomeAt(x, y);
                    if (b == null || (!b.name.equals("山地") && !b.name.equals("山谷") && !b.name.equals("荒地"))) continue;

                    float wx = x * 0.3f, wy = y * 0.3f;
                    float val = wallNoise.fbm(wx, wy + 20f, 0f, 3, 0.7f, 2f);
                    if (val > 0.40f) {
                        wallSeed[y][x] = true;
                    }
                }
            }
        }

        // 第二步：只要附近有 >= 2 个种子点就放墙
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (floorAt(x, y) != Blocks.stone) continue;
                Biome b = biomeSystem.getBiomeAt(x, y);
                if (b == null || (!b.name.equals("山地") && !b.name.equals("山谷") && !b.name.equals("荒地"))) continue;

                int count = 0;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        if (dx == 0 && dy == 0) continue;
                        int nx = x + dx, ny = y + dy;
                        if (nx >= 0 && nx < width && ny >= 0 && ny < height && wallSeed[ny][nx]) count++;
                    }
                }
                if (count >= 3) {
                    tiles.getn(x, y).setBlock(Blocks.stoneWall);
                }
            }
        }

        // 第三步：墙体扩张（4 轮）
        for (int expand = 0; expand < 4; expand++) {
            boolean[][] expandWall = new boolean[height][width];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (tiles.getn(x, y).block() == Blocks.stoneWall) {
                        expandWall[y][x] = true;
                        continue;
                    }
                    if (floorAt(x, y) != Blocks.stone) continue;
                    Biome b = biomeSystem.getBiomeAt(x, y);
                    if (b == null || (!b.name.equals("山地") && !b.name.equals("山谷") && !b.name.equals("荒地"))) continue;

                    int count = 0;
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            if (dx == 0 && dy == 0) continue;
                            int nx = x + dx, ny = y + dy;
                            if (nx >= 0 && nx < width && ny >= 0 && ny < height
                                && tiles.getn(nx, ny).block() == Blocks.stoneWall) count++;
                        }
                    }
                    if (count >= 1) expandWall[y][x] = true;
                }
            }
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (expandWall[y][x]) {
                        tiles.getn(x, y).setBlock(Blocks.stoneWall);
                    }
                }
            }
        }

        // 第四步：补充填充
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (floorAt(x, y) != Blocks.stone) continue;
                if (tiles.getn(x, y).block() == Blocks.stoneWall) continue;
                Biome b = biomeSystem.getBiomeAt(x, y);
                if (b == null || (!b.name.equals("山地") && !b.name.equals("山谷") && !b.name.equals("荒地"))) continue;

                boolean hasNearbyWall = false;
                for (int dy = -1; dy <= 1 && !hasNearbyWall; dy++) {
                    for (int dx = -1; dx <= 1 && !hasNearbyWall; dx++) {
                        if (dx == 0 && dy == 0) continue;
                        int nx = x + dx, ny = y + dy;
                        if (nx >= 0 && nx < width && ny >= 0 && ny < height
                            && tiles.getn(nx, ny).block() == Blocks.stoneWall) {
                            hasNearbyWall = true;
                        }
                    }
                }
                if (hasNearbyWall && scatterRng.nextFloat() < 0.15f) {
                    tiles.getn(x, y).setBlock(Blocks.stoneWall);
                }
            }
        }

        // ===== 散落墙体与巨石（只限山地/山谷/荒地）=====
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (floorAt(x, y) == Blocks.stone && tiles.getn(x, y).block() == Blocks.air) {
                    Biome b = biomeSystem.getBiomeAt(x, y);
                    if (b == null || (!b.name.equals("山地") && !b.name.equals("山谷") && !b.name.equals("荒地"))) continue;

                    float r = scatterRng.nextFloat();
                    if (r < 0.005f) {
                        tiles.getn(x, y).setBlock(Blocks.stoneWall);
                    } else if (r < 0.02f) {
                        tiles.getn(x, y).setBlock(Blocks.boulder);
                    }
                }
            }
        }

        blend(Blocks.stone, Blocks.sand, 3);
        distort(10f, 12f);

        // ===== 核心 =====
        int[] spawn = findSpawn(spawnX, spawnY, 25);
        spawnX = spawn[0];
        spawnY = spawn[1];

        // ===== 核心周围强制为平原/稀疏平原 =====
        int forceRadius = 18;
        for (int y = Math.max(0, spawnY - forceRadius); y <= Math.min(height - 1, spawnY + forceRadius); y++) {
            for (int x = Math.max(0, spawnX - forceRadius); x <= Math.min(width - 1, spawnX + forceRadius); x++) {
                float dist = Mathf.dst(x, y, spawnX, spawnY);
                if (dist <= forceRadius) {
                    Biome target;
                    if (dist < forceRadius * 0.4f) {
                        target = biomeSystem.biomes[0];
                    } else if (rand.chance(0.5f)) {
                        target = biomeSystem.biomes[0];
                    } else {
                        target = biomeSystem.biomes[1];
                    }
                    biomeSystem.biomeMap[y][x] = target;
                    float wx = x * 0.3f, wy = y * 0.3f;
                    float raw = biomeSystem.noise.fbm(
                        wx * target.heightFrequency, wy * target.heightFrequency, 0f, 5, 0.6f, 2f
                    );
                    biomeSystem.heightMap[y][x] = Mathf.clamp((raw + 1f) / 2f * target.heightAmplitude, 0f, 1f);
                    tiles.getn(x, y).setFloor((Floor) biomeSystem.getFloorAt(x, y));

                    if (dist < forceRadius * 0.3f) {
                        tiles.getn(x, y).setBlock(Blocks.air);
                    }
                }
            }
        }

        // ===== 核心放置 =====
        inverseFloodFill(tiles.getn(spawnX, spawnY));
        erase(spawnX, spawnY, 15);
        Schematics.placeLaunchLoadout(spawnX, spawnY);

        // ===== 敌人 =====
        int enemyCount = rand.random(5, 9);
        for (int i = 0; i < enemyCount; i++) {
            float a = 360f / enemyCount * i + rand.random(-20f, 20f);
            float d = Math.min(width, height) * 0.44f;
            int ex = Mathf.clamp((int) (cx + Angles.trnsx(a, d)), 5, width - 5);
            int ey = Mathf.clamp((int) (cy + Angles.trnsy(a, d)), 5, height - 5);
            Tile tile = tiles.getn(ex, ey);
            if (tile != null) {
                tile.setOverlay(Blocks.spawn);
                erase(ex, ey, 3);
            }
        }

        // ===== 矿物（留空，后续实现自定义矿物）=====
    }
}