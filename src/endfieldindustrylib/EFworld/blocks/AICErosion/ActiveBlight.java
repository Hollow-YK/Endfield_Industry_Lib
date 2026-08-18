package endfieldindustrylib.EFworld.blocks.AICErosion;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.gen.Building;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.BuildVisibility;

/**
 * 侵蚀墙体 — 仅通过四邻（上/下/左/右）的侵蚀核维持结构。
 * <p>
 * 采用数值传播法：每个墙体记录到最近核的"步数"（dist），
 * 失去核支撑后全部相连墙体级联重置为 INF，开始凋零。
 */
public class ActiveBlight extends Block {

    /** 距离上限，大于此值视为不可达 */
    private static final int INF_DIST = 999;

    public ActiveBlight(String name) {
        super(name);
        update = true;
        solid = true;
        destructible = true;
        breakable = false;      // 禁止右键拆除
        health = 400;
        size = 1;
        group = BlockGroup.walls;
        requirements(Category.defense, new ItemStack[0]);
        buildVisibility = BuildVisibility.editorOnly;   // 仅编辑器可放置，生存模式隐藏
        canPickup = false;                               // 禁止被荷载单位装载
    }

    @Override
    public boolean canBreak(Tile tile){
        return false;           // 强制不可被玩家拆除，仅通过凋零逻辑消亡
    }

    /**
     * 由 BlightCore 调用：从被摧毁的核相邻的墙体出发，
     * BFS 遍历所有相连的 ActiveBlight，将其 dist 级联重置为 INF_DIST。
     */
    public static void cascadeResetFrom(Building start) {
        if (!(start instanceof ActiveBlightBuild)) return;

        Seq<Building> queue = new Seq<>();
        queue.add(start);

        while (queue.size > 0) {
            Building current = queue.pop();
            if (!(current instanceof ActiveBlightBuild wall)) continue;
            if (wall.dist == INF_DIST) continue; // 已重置，跳过

            // 重置距离并强制下帧重新计算
            wall.dist = INF_DIST;
            wall.tickCounter = UPDATE_INTERVAL - 1;

            // 四个正交方向
            int[][] dirs = {{0,1},{0,-1},{-1,0},{1,0}};
            for (int[] d : dirs) {
                Building nb = wall.nearby(d[0], d[1]);
                if (nb instanceof ActiveBlightBuild nbWall && nbWall.dist != INF_DIST) {
                    queue.add(nb);
                }
            }
        }
    }

    // 距离每帧更新一次的频率（仅内部使用），暴露给 cascadeResetFrom
    private static final int UPDATE_INTERVAL = 5;

    public class ActiveBlightBuild extends Building {
        // 到最近侵蚀核的步数（四方向），INF_DIST 表示不可达
        private int dist = INF_DIST;

        // 更新计数器
        private int tickCounter = 0;

        // 凋零速度随机倍率（±30%），在放置时确定
        private float witherSpeedMul = 1f;

        @Override
        public void created() {
            witherSpeedMul = 1f + Mathf.range(0.3f);
        }

        @Override
        public void updateTile() {
            if (dead()) return;

            if (++tickCounter < UPDATE_INTERVAL) return;
            tickCounter = 0;

            // 重新计算自己的距离
            int newDist = computeDistance();

            if (newDist != this.dist) {
                this.dist = newDist;
            }

            // 每帧都根据当前 dist 决定是否凋零（不受距离变化检测限制）
            if (this.dist >= INF_DIST) {
                // ~5秒凋零完毕（400HP ÷ 80HP/s），±30% 随机偏差
                health -= 240f * witherSpeedMul * Time.delta / 60f;
                if (health <= 0f && !dead()) {
                    kill();
                }
            }
            // 恢复连通后只需停止扣血（else 分支自然跳过扣血逻辑）
        }

        /**
         * 计算到最近核的步数：遍历四邻，取邻居 dist + 1 的最小值；
         * 若邻居是 BlightCore 则直接返回 0。
         */
        private int computeDistance() {
            int minNeighborDist = INF_DIST;

            int[][] dirs = {{0,1},{0,-1},{-1,0},{1,0}};
            for (int[] d : dirs) {
                Building nb = nearby(d[0], d[1]);
                if (nb == null) continue;

                if (nb.block instanceof BlightCore) {
                    return 0;
                }

                if (nb.block instanceof ActiveBlight) {
                    int candidate = ((ActiveBlightBuild) nb).dist + 1;
                    if (candidate < minNeighborDist) {
                        minNeighborDist = candidate;
                    }
                }
            }

            return minNeighborDist;
        }

        @Override
        public float handleDamage(float amount) {
            return 0f; // 免疫所有武器/爆炸伤害
        }
    }
}
