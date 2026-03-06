package endfieldindustrylib.ui;

import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.scene.event.Touchable;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.graphics.Color;
import arc.Core;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.blocks.environment.StaticTree;
import mindustry.world.blocks.environment.StaticWall;
import mindustry.gen.Building;
import mindustry.Vars;
import arc.graphics.g2d.Draw;

public class Powerline {

    public List<int[]> findPath(int x, int y, int x2, int y2) {
        int width = Vars.world.width();
        int height = Vars.world.height();
        // 坐标边界检查
        if (x < 0 || x >= width || y < 0 || y >= height || x2 < 0 || x2 >= width || y2 < 0 || y2 >= height) {
            return new ArrayList<>();
        }
        // 距离数组，-1 表示未访问
        int[][] dist = new int[width][height];
        // 父节点坐标，用于回溯路径
        int[][] prevX = new int[width][height];
        int[][] prevY = new int[width][height];
        for (int i = 0; i < width; i++) {
            Arrays.fill(dist[i], -1);
        }

        Queue<int[]> queue = new LinkedList<int[]>();
        queue.offer(new int[] { x, y });
        dist[x][y] = 0;

        // 四方向移动（可根据需要改为八方向）
        int[] dx = { 1, -1, 0, 0 };
        int[] dy = { 0, 0, 1, -1 };
        int i = 0;
        boolean found = false;
        while (!queue.isEmpty() && i < 10000) {
            int[] cur = queue.poll();
            int cx = cur[0], cy = cur[1];
            i++;
            if (cx == x2 && cy == y2) {
                found = true;
                break;
            }

            for (int d = 0; d < 4; d++) {
                int nx = cx + dx[d];
                int ny = cy + dy[d];

                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    Tile tile = Vars.world.tile(nx, ny);
                    // 非固体且未访问过的格子才能扩展
                    if (tile != null && dist[nx][ny] == -1) {
                        if (!(tile.block() instanceof StaticWall) && !(tile.block() instanceof Wall)
                                && !(tile.block() instanceof StaticTree)) {
                            dist[nx][ny] = dist[cx][cy] + 1;
                            prevX[nx][ny] = cx;
                            prevY[nx][ny] = cy;
                            queue.offer(new int[] { nx, ny });
                        }

                    }
                }
            }
        }

        if (!found) {
            return new ArrayList<>(); // 不可达
        }

        // 回溯构造路径（从终点反向走到起点）
        List<int[]> path = new ArrayList<>();
        int cx = x2, cy = y2;
        while (cx != x || cy != y) {
            path.add(0, new int[] { cx, cy }); // 每次插入到列表开头
            int px = prevX[cx][cy];
            int py = prevY[cx][cy];
            cx = px;
            cy = py;
        }
        path.add(0, new int[] { x, y }); // 添加起点
        return path;
    }

    private List<int[]> lest = new ArrayList<>();

    public int drawpowerline(Building build, int plLength) {

        int mouseTileX = (int) Math.rint(Core.input.mouseWorldX() / Vars.tilesize);
        int mouseTileY = (int) Math.rint(Core.input.mouseWorldY() / Vars.tilesize);
        Building select = Vars.world.tile(mouseTileX, mouseTileY).build;
        if(select==null){
            lest = findPath((int) build.tileX(), (int) build.tileY(), mouseTileX, mouseTileY);
        }else{
            lest = findPath((int) build.tileX(), (int) build.tileY(), select.tileX(),select.tileY());
        }
        if (lest.isEmpty() || lest == null) {
            return -1;
        } else {
            Lines.stroke(2f);
            for (int i = 0; i < lest.size() - 1; i++) {
                Draw.z(100);
                if (lest.size() <= 60) {
                    Draw.color(Color.yellow);
                } else {
                    Draw.color(Color.red);
                }
                int[] current = lest.get(i);
                int[] next = lest.get(i + 1);

                // 将格子坐标转换为世界坐标 (每个格子大小为 tilesize，通常为 8px)
                float x1 = current[0] * Vars.tilesize;
                float y1 = current[1] * Vars.tilesize;
                float x2 = next[0] * Vars.tilesize;
                float y2 = next[1] * Vars.tilesize;

                // 画线
                Lines.line(x1, y1, x2, y2);

                // 在格点画个小圆标记
                Draw.color(Color.white);
                Fill.circle(x1, y1, 1.25f);
            }
        }
        Draw.reset();
        return lest.size();
    }
    private static Table tablename= new Table();
    public void showText(Label label){
        tablename.clearChildren();    
        tablename.add(label).pad(4f);
        label.setStyle(mindustry.ui.Styles.outlineLabel);    
        tablename.setFillParent(false); // 不填充父级，手动控制位置
        tablename.touchable = Touchable.disabled;
        Core.scene.add(tablename);
    }
    public void fallowMouse(){
        tablename.setPosition(Core.input.mouseX(), Core.input.mouseY() + 15);
    }
    public void removeText(){
        tablename.remove();
    }
    public int canpowerline(Building build, Building other) {
        lest = findPath((int) build.tileX(), (int) build.tileY(), (int) other.tileX(), (int) other.tileY());
        if (lest.isEmpty() || lest == null) {
            return -1;
        } else {
            return lest.size();
        }
    }
}