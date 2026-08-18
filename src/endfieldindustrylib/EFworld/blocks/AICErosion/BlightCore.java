package endfieldindustrylib.EFworld.blocks.AICErosion;

import arc.struct.Seq;
import arc.util.Time;
import static mindustry.Vars.control;
import static mindustry.Vars.player;
import static mindustry.Vars.state;
import static mindustry.Vars.world;
import mindustry.ai.UnitCommand;
import mindustry.content.Blocks;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.BuildVisibility;

/**
 * 侵蚀核 — 侵蚀墙体的结构支撑核心。
 * <p>
 * 行为类似 Minecraft 的原木：为周围的 ActiveBlight 提供结构支撑。
 * 当核被摧毁时，相邻的墙体会失去支撑，开始凋零崩解。
 * <p>
 * 只有建造速度（{@link mindustry.type.UnitType#buildSpeed buildSpeed}）
 * ≥ {@link #MIN_BUILD_SPEED} 的单位才能通过拆除指令摧毁核。
 */
public class BlightCore extends Block {

    /** 拆除核所需的最低建造速度 */
    public static final float MIN_BUILD_SPEED = 3f;

    public BlightCore(String name) {
        super(name);
        update = true;
        solid = true;
        destructible = true;
        breakable = true;       // 允许被选为拆除目标
        health = 600;
        size = 1;
        group = BlockGroup.walls;
        requirements(Category.defense, new ItemStack[0]);
        buildVisibility = BuildVisibility.editorOnly;   // 仅编辑器可放置，生存模式隐藏
        canPickup = false;                               // 禁止被荷载单位装载
        // 拆除时间：buildSpeed 1.0 时 60s，塔塔（buildSpeed 4.0）时 15s
        buildTime = 3600;
    }

    public class BlightCoreBuild extends Building {

        @Override
        public void updateTile() {
            // 当前无周期性逻辑，预留接口
        }

        /**
         * 当核被摧毁/移除时，通知四邻的侵蚀墙体重置其距离值。
         */
        @Override
        public void onDestroyed() {
            super.onDestroyed();
            notifyWallsToReset();
        }

        /**
         * 建造速度检查 — 在游戏将方块替换为 ConstructBlock 之前被调用。
         * <p>
         * 沙盒模式（{@code state.rules.infiniteResources}）跳过限制，
         * 否则玩家单位建造速度不足会导致核被复原，墙壁收不到通知。
         * <p>
         * 墙体凋零不在此时触发，而是等 ConstructBlock 拆除完毕、
         * 瓦片变为空地后才启动，见下方的延迟检测。
         */
        @Override
        public void onDeconstructed(Unit unit) {
            boolean sandbox = state.rules.infiniteResources;
            // 检查发起单位自身建造速度
            boolean speedOk = unit != null && unit.type != null && unit.type.buildSpeed >= MIN_BUILD_SPEED;

            // 若发起单位速度不足，检查玩家自身和指挥队列中的单位
            if (!sandbox && !speedOk) {
                speedOk = playerHasQualifiedUnit();
            }

            if (!sandbox && !speedOk) {
                // 条件不满足 → 下帧将 ConstructBlock 恢复为侵蚀核
                var saveTile = this.tile;
                var saveBlock = this.block;
                var saveTeam = this.team;
                var saveRot = this.rotation;
                Time.runTask(0f, () -> {
                    if (saveTile != null) {
                        saveTile.setBlock(saveBlock, saveTeam, saveRot);
                    }
                });
                return; // 不调 super — 不执行液体溢出等清理
            }

            // 保存核的瓦片坐标和四邻墙体位置，用于延迟级联
            int coreX = tile.x, coreY = tile.y;
            int[][] dirs = {{0,1},{0,-1},{-1,0},{1,0}};
            Seq<int[]> wallPositions = new Seq<>();
            for (int[] d : dirs) {
                Building nb = nearby(d[0], d[1]);
                if (nb != null && nb.block instanceof ActiveBlight) {
                    wallPositions.add(new int[]{nb.tileX(), nb.tileY()});
                }
            }

            // 允许 beginBreak 继续（方块被替换为 ConstructBlock）
            super.onDeconstructed(unit);

            // 延迟检测：每帧检查瓦片，直到 ConstructBlock 完全消失后才启动墙体凋零
            Time.runTask(0f, new Runnable() {
                @Override
                public void run() {
                    Tile t = world.tile(coreX, coreY);
                    if (t == null || t.block() == Blocks.air) {
                        // 核已完全拆除 → 级联重置所有相连墙体
                        for (int[] pos : wallPositions) {
                            Building b = world.build(pos[0], pos[1]);
                            if (b != null && b.block instanceof ActiveBlight) {
                                ActiveBlight.cascadeResetFrom(b);
                            }
                        }
                    } else {
                        // 仍在拆除中，下帧继续检查
                        Time.runTask(0f, this);
                    }
                }
            });
        }

        /**
         * 检查以下三种情况中是否有符合资格的建造单位：
         * ① 玩家直接控制的单位
         * ② 指挥模式下选中的单位队列
         * ③ 处于协助建造模式且在建造范围内的友方单位
         */
        private boolean playerHasQualifiedUnit() {
            float coreX = getX(), coreY = getY();

            // 1) 玩家直接控制的单位
            Unit pu = player.unit();
            if (checkUnit(pu, coreX, coreY)) return true;

            // 2) 指挥模式下选中的单位队列
            for (Unit u : control.input.selectedUnits) {
                if (checkUnit(u, coreX, coreY)) return true;
            }

            // 3) 协助建造模式下的友方单位（可能不在选中队列但处于跟随协助状态）
            boolean[] found = {false};
            Units.nearby(player.team(), coreX, coreY, 50f, u -> {
                if (!found[0]
                    && u.isCommandable()
                    && u.command().command == UnitCommand.assistCommand
                    && u.type != null
                    && u.type.buildSpeed >= MIN_BUILD_SPEED
                    && u.within(coreX, coreY, u.type.buildRange)) {
                    found[0] = true;
                }
            });
            return found[0];
        }

        private boolean checkUnit(Unit u, float cx, float cy) {
            return u != null && u.type != null
                && u.type.buildSpeed >= MIN_BUILD_SPEED
                && u.within(cx, cy, u.type.buildRange);
        }

        /** 从四邻墙体出发，级联重置所有相连墙体的距离 */
        private void notifyWallsToReset() {
            int[][] dirs = {{0,1},{0,-1},{-1,0},{1,0}};
            for (int[] d : dirs) {
                Building nb = nearby(d[0], d[1]);
                if (nb != null && nb.block instanceof ActiveBlight) {
                    ActiveBlight.cascadeResetFrom(nb);
                }
            }
        }

        @Override
        public boolean onConfigureBuildTapped(Building other) {
            return false;
        }
    }
}
