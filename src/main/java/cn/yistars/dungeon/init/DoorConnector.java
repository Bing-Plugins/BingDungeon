package cn.yistars.dungeon.init;

import cn.yistars.dungeon.room.Room;
import cn.yistars.dungeon.room.door.Door;
import lombok.Getter;

import java.awt.*;
import java.util.*;
import java.util.List;

@Getter
public class DoorConnector {
    private final ArrayList<Room> rooms;
    private final HashSet<Point> result = new HashSet<>(); // 结果
    private final HashMap<Point, List<Point>> doorConnections = new HashMap<>(); // 门口与其连接点
    private Set<Rectangle> obstacles = new HashSet<>(); // 所有障碍物
    private List<Point> allDoors = new ArrayList<>(); // 所有门

    // 方向: 上、右、下、左
    private final int[][] directions = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};

    public DoorConnector(ArrayList<Room> rooms) {
        this.rooms = rooms;
        initializeObstaclesAndDoors();
        connectAll();
    }

    /**
     * 初始化所有障碍物和门
     */
    private void initializeObstaclesAndDoors() {
        // 收集所有障碍物和门
        for (Room room : rooms) {
            obstacles.add(room.getRectangle());
            for (Door door : room.getDoors()) {
                Point doorPoint = door.getPoint();
                allDoors.add(doorPoint);
                result.add(doorPoint); // 添加门点到结果中
                doorConnections.put(doorPoint, new ArrayList<>());
            }
        }
    }

    /**
     * 连接所有门
     */
    private void connectAll() {
        // 使用蚁群算法选择最佳连接顺序
        List<Point> optimizedOrder = antColonyOptimization();

        // 连接所有门
        for (int i = 0; i < optimizedOrder.size() - 1; i++) {
            Point start = optimizedOrder.get(i);
            Point end = optimizedOrder.get(i + 1);

            // 如果两个门已经通过其他路径连接，则跳过
            if (areConnected(start, end)) {
                continue;
            }

            // 使用A*算法寻找从start到end的路径
            List<Point> path = findPathAStar(start, end);

            if (path != null) {
                // 添加路径到结果中
                for (Point p : path) {
                    result.add(p);
                }

                // 更新门的连接关系
                doorConnections.get(start).add(end);
                doorConnections.get(end).add(start);
            }
        }

        // 检查是否所有门都已连接，若有未连接的门，尝试连接到最近的路径
        connectRemainingDoors();
    }

    /**
     * 检查两个门是否已经通过路径连接
     */
    private boolean areConnected(Point start, Point end) {
        // 使用BFS检查两点是否已连接
        Queue<Point> queue = new LinkedList<>();
        Set<Point> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Point current = queue.poll();

            if (current.equals(end)) {
                return true;
            }

            // 检查四个方向
            for (int[] dir : directions) {
                Point next = new Point(current.x + dir[0], current.y + dir[1]);
                if (result.contains(next) && !visited.contains(next)) {
                    queue.add(next);
                    visited.add(next);
                }
            }
        }

        return false;
    }

    /**
     * 使用蚁群优化算法确定连接门的最佳顺序
     */
    private List<Point> antColonyOptimization() {
        AntColonyOptimizer optimizer = new AntColonyOptimizer(allDoors, obstacles);
        return optimizer.findOptimalOrder();
    }

    /**
     * 使用A*算法寻找两点间的路径
     */
    private List<Point> findPathAStar(Point start, Point end) {
        AStar aStar = new AStar(obstacles, result);
        return aStar.findPath(start, end);
    }

    /**
     * 连接剩余未连接的门
     */
    private void connectRemainingDoors() {
        // 对于每个门，检查是否与其他门连接
        for (Point door : allDoors) {
            if (!isConnectedToAnyDoor(door)) {
                // 寻找最近的已有路径点
                Point nearestPathPoint = findNearestPathPoint(door);
                if (nearestPathPoint != null) {
                    List<Point> path = findPathAStar(door, nearestPathPoint);
                    if (path != null) {
                        for (Point p : path) {
                            result.add(p);
                        }
                    }
                }
            }
        }
    }

    /**
     * 检查一个门是否与任何其他门连接
     */
    private boolean isConnectedToAnyDoor(Point door) {
        return !doorConnections.get(door).isEmpty();
    }

    /**
     * 寻找最近的已有路径点
     */
    private Point findNearestPathPoint(Point door) {
        Point nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Point p : result) {
            // 跳过门本身
            if (allDoors.contains(p)) continue;

            double distance = door.distance(p);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = p;
            }
        }

        return nearest;
    }
}