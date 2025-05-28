package cn.yistars.dungeon.init.grid;

import lombok.Getter;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

@Getter
public class GridGenerator {
    private final int GRID_RADIUS = 12; // 需要为偶数
    private final Random random = new Random();
    private final HashSet<Point> finalGrid = new HashSet<>();

    public GridGenerator() {
        // 生成初始网状带孔结构
        HashSet<GridPoint> initialGrid = generateMeshWithHoles();
        System.out.println("初始网格点数量: " + initialGrid.size());

        // 随机挖去其中的 'a' 点
        HashSet<GridPoint> removeGrid = randomlyRemovePoints(initialGrid);
        System.out.println("最终网格点数量: " + removeGrid.size());

        // 随机覆盖矩形
        HashSet<GridPoint> rectangleGrid = removeRectangle(removeGrid);
        System.out.println("移除矩形后网格点数量: " + rectangleGrid.size());

        for (GridPoint p : rectangleGrid) {
            if (!p.getType().equals(PointType.O)) finalGrid.add(p.getPoint());
        }

        // 可视化展示（可选）
        visualizeGrid(finalGrid);
    }

    /**
     * 生成31×31的网状带孔结构
     * 按照模式：x x x x x
     *         x o a o x
     *         x a x a x
     *         x o a o x
     *         x x x x x
     */
    public HashSet<GridPoint> generateMeshWithHoles() {
        HashSet<GridPoint> points = new HashSet<>();

        // 第一行和最后一行的横线
        for (int x = -GRID_RADIUS; x <= GRID_RADIUS; x++) {
            points.add(new GridPoint(new Point(x, -GRID_RADIUS), PointType.X));
            points.add(new GridPoint(new Point(x, GRID_RADIUS), PointType.X));
        }
        // 第一列和最后一列的竖线
        for (int y = -GRID_RADIUS; y <= GRID_RADIUS; y++) {
            points.add(new GridPoint(new Point(-GRID_RADIUS, y), PointType.X));
            points.add(new GridPoint(new Point(GRID_RADIUS, y), PointType.X));
        }

        // 中间部分的网状带孔结构
        for (int x = -GRID_RADIUS + 1; x < GRID_RADIUS; x++) {
            for (int y = -GRID_RADIUS + 1; y < GRID_RADIUS; y++) {
                if (x % 2 == 0 && y % 2 == 0) {
                    // 添加 'x' 点
                    points.add(new GridPoint(new Point(x, y), PointType.X));
                } else if (x % 2 == 0 || y % 2 == 0) {
                    // 添加 'a' 点
                    points.add(new GridPoint(new Point(x, y), PointType.A));
                } else {
                    // 添加 'o' 点
                    points.add(new GridPoint(new Point(x, y), PointType.O));
                }
            }
        }

        return points;
    }

    /**
     * 随机移除网格中的一些点（相当于挖去其中的'a'点）
     * @param originalGrid 原始网格
     * @return 移除部分点后的新网格
     */
    public HashSet<GridPoint> randomlyRemovePoints(HashSet<GridPoint> originalGrid) {
        // 改为 HashMap
        HashMap<Point, GridPoint> gridMap = new HashMap<>();
        for (GridPoint point : originalGrid) {
            gridMap.put(point.getPoint(), point);
        }

        // 随机移除30%的类型为 a 的点
        int pointsToRemove = (int) (gridMap.size() * 0.3); // 移除30%的点
        int removedCount = 0;
        for (Point point : gridMap.keySet()) {
            if (gridMap.get(point).getType() == PointType.A && removedCount < pointsToRemove) {
                // 将点设置为 'o'
                gridMap.put(point, new GridPoint(point, PointType.O));
                removedCount++;
            }
        }

        // 将 HashMap 转换回 HashSet
        HashSet<GridPoint> newGrid = new HashSet<>(gridMap.values());
        return newGrid;
    }

    /**
     * 移除部分矩形区域
     */
    public HashSet<GridPoint> removeRectangle(HashSet<GridPoint> originalGrid) {
        // 改为 HashMap
        HashMap<Point, GridPoint> gridMap = new HashMap<>();
        for (GridPoint point : originalGrid) {
            gridMap.put(point.getPoint(), point);
        }
        /*
        // 随机选择 2~4
        int rectCount = 2 + random.nextInt(3); // 随机选择 2 到 4 个矩形区域
        // 随机选择左上、右上、左下、右下的其中 rectCount 个位置，移除偶数边长的 Point
        HashSet<DirectionType> directions = new HashSet<>();
        while (directions.size() < rectCount) {
            DirectionType direction = DirectionType.values()[random.nextInt(DirectionType.values().length)];
            directions.add(direction);
        }
        // 遍历方向并移除对应的矩形区域
        for (DirectionType direction : directions) {
            // 随机偶数矩形边长，最大为 8
            int rectSize = 2 + 2 * (random.nextInt(GRID_SIZE / 2 - 1)); // 确保是偶数
            switch (direction) {
                case UP_LEFT:
                    // 移除左上角矩形
                    for (int x = -GRID_SIZE; x < -GRID_SIZE + rectSize; x++) {
                        for (int y = -GRID_SIZE; y < -GRID_SIZE + rectSize; y++) {
                            gridMap.put(new Point(x, y), new GridPoint(new Point(x, y), PointType.O));
                        }
                    }
                    break;
                case UP_RIGHT:
                    // 移除右上角矩形
                    for (int x = GRID_SIZE - rectSize; x <= GRID_SIZE; x++) {
                        for (int y = -GRID_SIZE; y < -GRID_SIZE + rectSize; y++) {
                            gridMap.put(new Point(x, y), new GridPoint(new Point(x, y), PointType.O));
                        }
                    }
                    break;
                case DOWN_LEFT:
                    // 移除左下角矩形
                    for (int x = -GRID_SIZE; x < -GRID_SIZE + rectSize; x++) {
                        for (int y = GRID_SIZE - rectSize; y <= GRID_SIZE; y++) {
                            gridMap.put(new Point(x, y), new GridPoint(new Point(x, y), PointType.O));
                        }
                    }
                    break;
                case DOWN_RIGHT:
                    // 移除右下角矩形
                    for (int x = GRID_SIZE - rectSize; x <= GRID_SIZE; x++) {
                        for (int y = GRID_SIZE - rectSize; y <= GRID_SIZE; y++) {
                            gridMap.put(new Point(x, y), new GridPoint(new Point(x, y), PointType.O));
                        }
                    }
                    break;
            }
        }*/

        // 随机挑 1~6 个点
        /*
        int pointsToRemove = 1 + random.nextInt(6);
        for (int i = 0; i < pointsToRemove; i++) {
            // 随机选择一个点，要求为 x 和 y 为奇数
            int x = -GRID_RADIUS + 1 + 2 * random.nextInt((GRID_RADIUS - 1) / 2);
            int y = -GRID_RADIUS + 1 + 2 * random.nextInt((GRID_RADIUS - 1) / 2);
            // 随机矩形大小，最大为 4
            int rectSize = 2 + 2 * random.nextInt(2); // 确保是偶数
            // 设置点位为 o
            for (int dx = 0; dx < rectSize; dx++) {
                for (int dy = 0; dy < rectSize; dy++) {
                    int newX = x + dx;
                    int newY = y + dy;
                    // 区域不能包括 (-2, -2) 到 (2, 2) 的区域
                    if (newX >= -2 && newX <= 2 && newY >= -2 && newY <= 2) continue;
                    // 确保点在网格范围内
                    if (gridMap.containsKey(new Point(newX, newY))) {
                        // 将点设置为 'o'
                        gridMap.put(new Point(newX, newY), new GridPoint(new Point(newX, newY), PointType.O));
                    }
                }
            }
        }*/

        HashSet<GridPoint> newGrid = new HashSet<>(gridMap.values());
        return newGrid;
    }

    /**
     * 可视化显示网格（可选方法，用于调试）
     */
    public void visualizeGrid(HashSet<Point> grid) {
        System.out.println("\n网格可视化（显示前15×15区域）:");
        for (int y = 0; y < Math.min(15, GRID_RADIUS); y++) {
            for (int x = 0; x < Math.min(15, GRID_RADIUS); x++) {
                if (grid.contains(new Point(x, y))) {
                    System.out.print("■ ");
                } else {
                    System.out.print("□ ");
                }
            }
            System.out.println();
        }
    }
}