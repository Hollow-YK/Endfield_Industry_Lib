package endfieldindustrylib.EFworld;

import arc.struct.Seq;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.world.Tile;

public class EFBuildings {
    /**
     * 获取指定矩形范围内的所有建筑。
     * @param x1 左下角X坐标（格子坐标）
     * @param y1 左下角Y坐标（格子坐标）
     * @param x2 右上角X坐标（格子坐标）
     * @param y2 右上角Y坐标（格子坐标）
     * @return 包含范围内所有建筑的Seq列表（可能为空）
     */
    public static Seq<Building> getBuildingsInRect(int x1, int y1, int x2, int y2) {
        // 确保坐标顺序正确（min到max）
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);

        Seq<Building> result = new Seq<>();

        // 遍历矩形内的所有格子
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                Tile tile = Vars.world.tile(x, y);
                if (tile != null && tile.build != null) {
                    result.add(tile.build);
                }
            }
        }
        return result;
    }
}