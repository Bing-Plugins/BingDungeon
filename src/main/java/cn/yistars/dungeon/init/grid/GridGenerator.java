package cn.yistars.dungeon.init.grid;

import cn.yistars.dungeon.BingDungeon;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.awt.Point;
import java.util.*;

public class GridGenerator {
    private final ConfigurationSection config = Objects.requireNonNull(BingDungeon.instance.getConfig().getConfigurationSection("path-only"));
    private final int GRID_RADIUS = config.getInt("grid-radius", 18); // 需要为偶数
    private final Random random = new Random();
    @Getter
    private final HashSet<Point> finalGrid = new HashSet<>();

    public GridGenerator() {
        // 生成初始网状带孔结构
        HashSet<GridPoint> initialGrid = generateMeshWithHoles();
        System.out.println("初始网格点数量: " + initialGrid.size());

        // 随机覆盖矩形
        HashSet<GridPoint> rectangleGrid = removeRectangle(initialGrid);
        System.out.println("移除矩形后网格点数量: " + rectangleGrid.size());

        // 随机挖去其中的 'a' 点
        HashSet<GridPoint> removeGrid = randomlyRemovePoints(rectangleGrid);
        System.out.println("最终网格点数量: " + removeGrid.size());

        // 移除不可达点位
        HashSet<GridPoint> reachableGrid = removeUnreachablePoints(removeGrid);
        System.out.println("可达网格点数量: " + reachableGrid.size());

        for (GridPoint p : reachableGrid) {
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
        // 随机移除12.5%的类型为 a 的点
        int pointsToRemove = (int) (originalGrid.size() * config.getDouble("random-remove-point-probability", 0.125));
        int removedCount = 0;
        ArrayList<GridPoint> points = new ArrayList<>(originalGrid);
        Collections.shuffle(points);
        for (GridPoint point : points) {
            if (removedCount >= pointsToRemove) break;
            if (point.getType() == PointType.A) {
                // 将点设置为 'o'
                originalGrid.remove(point);
                removedCount++;
            }
        }

        // 将 HashMap 转换回 HashSet
        return originalGrid;
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

        // 随机选择 12~36 个矩形区域
        int rectCount = config.getInt("random-remove-rectangle-min", 24) + random.nextInt(config.getInt("random-remove-rectangle-range", 24)); // 随机选择 12 到 36 个矩形区域
        // 可选边长
        List<Integer> rectSizes = config.getIntegerList("random-remove-rectangle-sizes");
        // 除了绝对值为 1 的边长
        ArrayList<Integer> validSizes = new ArrayList<>();
        for (int size : rectSizes) if (Math.abs(size) != 1) validSizes.add(size);

        // 奇数点位
        for (int i = 0; i < rectCount; i++) {
            // 随机选择一个矩形区域的左上角点
            int x = -GRID_RADIUS + 2 * random.nextInt(GRID_RADIUS - 1);
            int y = -GRID_RADIUS + 2 * random.nextInt(GRID_RADIUS - 1);
            // 随机矩形大小
            int rectSizeX = rectSizes.get(random.nextInt(rectSizes.size()));
            int rectSizeY;
            // 如果 x 绝对值为 1，则随机选择一个有效的矩形大小
            if (Math.abs(rectSizeX) == 1) {
                rectSizeY = validSizes.get(random.nextInt(validSizes.size()));
            } else {
                rectSizeY = rectSizes.get(random.nextInt(rectSizes.size()));
            }
            System.out.println("随机矩形区域左上角: (" + x + ", " + y + "), 大小: (" + rectSizeX + ", " + rectSizeY + ")");
            // 设置点位为 o
            for (int dx = rectSizeX; Math.abs(dx) > 0; dx = dx > 0 ? dx - 1 : dx + 1) {
                for (int dy = rectSizeY; Math.abs(dy) > 0; dy = dy > 0 ? dy - 1 : dy + 1) {
                    int newX = x + dx;
                    int newY = y + dy;
                    Point point = new Point(newX, newY);
                    // 区域不能包括 (-2, -2) 到 (2, 2) 的区域
                    if (newX >= -2 && newX <= 2 && newY >= -2 && newY <= 2) continue;
                    // 确保点在网格范围内
                    if (!gridMap.containsKey(point)) continue;
                    // 将点设置为 'o'
                    GridPoint gridPoint = gridMap.get(point);
                    gridPoint.setType(PointType.O);
                    gridMap.put(point, gridPoint);
                    System.out.println("<UNK>: (" + newX + ", " + newY + ")");
                }
            }
        }

        // 随机选择 4~8
        int edgeCount = config.getInt("random-remove-edge-rectangle-min", 4) + random.nextInt(config.getInt("random-remove-edge-rectangle-range", 4)); // 随机选择 4 到 8 个矩形区域

        ArrayList<DirectionType> directions = new ArrayList<>(Arrays.asList(DirectionType.values()));

        for (int i = 0; i < edgeCount; i++) {
            directions.add(DirectionType.values()[random.nextInt(DirectionType.values().length)]);
        }

        for (DirectionType direction : directions) {
            // 获取随机区域
            HashSet<Point> region = direction.getRandomRegion(random, GRID_RADIUS);
            System.out.println("随机区域: " + region);
            // 设置点位为 o
            for (Point point : region) {
                // 确保点在网格范围内
                if (!gridMap.containsKey(point)) continue;
                // 将点设置为 'o'
                GridPoint gridPoint = gridMap.get(point);
                gridPoint.setType(PointType.O);
                gridMap.put(point, gridPoint);
            }
        }

        return new HashSet<>(gridMap.values());
    }

    private HashSet<GridPoint> removeUnreachablePoints(HashSet<GridPoint> originalGrid) {
        HashSet<GridPoint> reachablePoints = new HashSet<>();

        for (GridPoint point : originalGrid) {
            // 如果点是 'o' 类型，直接添加到可达点集合
            if (point.getType() == PointType.O) {
                reachablePoints.add(point);
                continue;
            }
            if (findPathToOrigin(originalGrid, point)) {
                reachablePoints.add(point);
            }
        }

        return reachablePoints;
    }

    /**
     * 获取从指定点到原点(0,0)的路径
     * @param grid 整个网格点集合
     * @param startPoint 起始点
     * @return 是否存在可行走的路径
     */
    public boolean findPathToOrigin(HashSet<GridPoint> grid, GridPoint startPoint) {
        // 如果起始点类型既不是o也不是x，直接返回false
        if (startPoint.getType() != PointType.A && startPoint.getType() != PointType.X) {
            return false;
        }
        
        // 创建点位到GridPoint的映射，便于快速查找
        Map<Point, GridPoint> gridMap = new HashMap<>();
        for (GridPoint gp : grid) {
            gridMap.put(gp.getPoint(), gp);
        }
        
        // 原点
        Point origin = new Point(0, 0);
        
        // 如果起始点就是原点，直接返回true
        if (startPoint.getPoint().equals(origin)) {
            return true;
        }
        
        // 使用BFS查找路径
        Queue<Point> queue = new LinkedList<>();
        Map<Point, Point> parentMap = new HashMap<>(); // 用于重建路径
        Set<Point> visited = new HashSet<>();
        
        queue.offer(startPoint.getPoint());
        visited.add(startPoint.getPoint());
        
        // 四个方向：上、下、左、右
        int[] dx = {0, 0, -1, 1};
        int[] dy = {1, -1, 0, 0};
        
        boolean pathFound = false;
        
        while (!queue.isEmpty() && !pathFound) {
            Point current = queue.poll();
            
            // 检查四个方向
            for (int i = 0; i < 4; i++) {
                Point next = new Point(current.x + dx[i], current.y + dy[i]);
                
                // 如果找到原点，记录路径并返回true
                if (next.equals(origin)) {
                    parentMap.put(next, current);
                    pathFound = true;
                    break;
                }
                
                // 如果下一个点在网格中，类型为o或x，且未访问过
                if (gridMap.containsKey(next) && 
                    (gridMap.get(next).getType() == PointType.A ||
                     gridMap.get(next).getType() == PointType.X) && 
                    !visited.contains(next)) {
                    
                    queue.offer(next);
                    visited.add(next);
                    parentMap.put(next, current);
                }
            }
        }
        
        // 如果找到路径，可以重建路径（可选）
        if (pathFound) {
            List<Point> path = new ArrayList<>();
            Point current = origin;
            
            // 从终点回溯到起点
            while (current != null) {
                path.add(0, current);
                current = parentMap.get(current);
            }
            
            // 打印路径（可选）
            // System.out.println("Path found: " + path);
        }
        
        return pathFound;
    }

    /**
     * 可视化显示网格（可选方法，用于调试）
     */
    public void visualizeGrid(HashSet<Point> grid) {
        System.out.println("网格可视化:");
        for (int y = -GRID_RADIUS; y <= Math.min(30, GRID_RADIUS); y++) {
            for (int x = -GRID_RADIUS; x <= Math.min(30, GRID_RADIUS); x++) {
                if (grid.contains(new Point(x, -y))) {
                    System.out.print("■ ");
                } else {
                    System.out.print("□ ");
                }
            }
            System.out.println();
        }
    }
}
