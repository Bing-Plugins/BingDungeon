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
    private final HashSet<Rectangle> obstacles = new HashSet<>(); // 所有的障碍物
    private final List<Door> allDoors = new ArrayList<>(); // 所有的门
    private final AStarPathFinder aStarPathFinder; // A*寻路算法
    private final ACOPathFinder acoPathFinder; // 蚁群优化算法

    public DoorConnector(ArrayList<Room> rooms) {
        this.rooms = rooms;

        // 初始化障碍物和门
        initObstaclesAndDoors();

        // 初始化寻路算法
        aStarPathFinder = new AStarPathFinder(obstacles);
        acoPathFinder = new ACOPathFinder(obstacles);

        // 执行路径连接
        connectDoors();
    }

    /**
     * 初始化障碍物和门信息
     */
    private void initObstaclesAndDoors() {
        for (Room room : rooms) {
            // 添加当前房间的障碍物
            obstacles.add(room.getRectangle());

            // 添加当前房间的所有门
            allDoors.addAll(room.getDoors());
        }
    }

    /**
     * 连接所有的门
     */
    private void connectDoors() {
        // 构建最小生成树保证所有Room都连接上
        List<DoorPair> mst = buildMinimumSpanningTree();

        // 对于每个门对，执行路径连接
        for (DoorPair doorPair : mst) {
            connectDoorPair(doorPair.door1, doorPair.door2);
        }
    }

    /**
     * 构建最小生成树，确保所有Room都连接上
     */
    private List<DoorPair> buildMinimumSpanningTree() {
        List<DoorPair> mst = new ArrayList<>();
        Set<Room> connectedRooms = new HashSet<>();
        Set<Door> connectedDoors = new HashSet<>();

        // 从第一个房间开始
        if (!rooms.isEmpty()) {
            connectedRooms.add(rooms.getFirst());
        }

        // 直到所有房间都连接上
        while (connectedRooms.size() < rooms.size()) {
            DoorPair bestPair = null;
            double bestDistance = Double.MAX_VALUE;

            // 寻找最近的连接点
            for (Door door1 : allDoors) {
                if (connectedDoors.contains(door1)) continue;

                Room room1 = door1.getRoom();
                if (!connectedRooms.contains(room1)) continue;

                for (Door door2 : allDoors) {
                    if (door1 == door2 || connectedDoors.contains(door2)) continue;

                    Room room2 = door2.getRoom();
                    if (room1 == room2) continue; // 避免同一房间门连接
                    if (connectedRooms.contains(room2)) continue; // 避免形成环

                    double distance = calculateManhattanDistance(door1.getPoint(), door2.getPoint());
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        bestPair = new DoorPair(door1, door2);
                    }
                }
            }

            if (bestPair != null) {
                mst.add(bestPair);
                connectedDoors.add(bestPair.door1);
                connectedDoors.add(bestPair.door2);
                connectedRooms.add(bestPair.door2.getRoom());
            } else {
                // 无法继续连接
                break;
            }
        }

        // 添加剩余的门连接，确保所有门都在路径上
        for (Door door : allDoors) {
            if (connectedDoors.contains(door)) continue;

            Door closestDoor = null;
            double minDistance = Double.MAX_VALUE;

            for (Door connectedDoor : connectedDoors) {
                if (door.getRoom() == connectedDoor.getRoom()) continue; // 避免同一房间门连接

                double distance = calculateManhattanDistance(door.getPoint(), connectedDoor.getPoint());
                if (distance < minDistance) {
                    minDistance = distance;
                    closestDoor = connectedDoor;
                }
            }

            if (closestDoor != null) {
                mst.add(new DoorPair(door, closestDoor));
                connectedDoors.add(door);
            }
        }

        return mst;
    }

    /**
     * 连接一对门
     */
    private void connectDoorPair(Door door1, Door door2) {
        Point start = door1.getPoint();
        Point end = getBestPointForConnection(start, door2).getFirst(); // 获取最适合连接的点

        // 将开始和结束点添加到结果中
        result.add(start);
        result.add(end);

        // 先尝试使用A*算法
        List<Point> path = aStarPathFinder.findPath(start, end);

        // 如果A*找不到路径或者路径较差，尝试蚁群算法
        //if (path == null || path.isEmpty() || pathQualityBelowThreshold(path)) {
            path = acoPathFinder.findPath(start, end);
        //}

        // 简化路径，去掉不必要的拐点
        path = simplifyPath(path);

        // 添加路径点到结果集
        result.addAll(path);
    }

    /**
     * 计算曼哈顿距离
     */
    private double calculateManhattanDistance(Point p1, Point p2) {
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    }

    /**
     * 判断路径质量是否低于阈值
     */
    private boolean pathQualityBelowThreshold(List<Point> path) {
        // 简单实现：如果拐点过多，则认为质量较低
        int turns = 0;
        for (int i = 1; i < path.size() - 1; i++) {
            Point prev = path.get(i-1);
            Point curr = path.get(i);
            Point next = path.get(i+1);

            if ((prev.x != curr.x && curr.x != next.x) ||
                    (prev.y != curr.y && curr.y != next.y)) {
                turns++;
            }
        }
        return turns > path.size() / 3; // 如果拐点超过路径长度的1/3
    }

    /**
     * 简化路径，删除田字形路径，转换为L形
     */
    private List<Point> simplifyPath(List<Point> path) {
        if (path == null || path.size() <= 2) return path;

        List<Point> result = new ArrayList<>();
        result.add(path.get(0)); // 添加起点

        for (int i = 1; i < path.size() - 1; i++) {
            Point prev = path.get(i-1);
            Point curr = path.get(i);
            Point next = path.get(i+1);

            // 如果当前点形成田字形路径的一部分，跳过
            if ((prev.x == curr.x && curr.y == next.y) ||
                    (prev.y == curr.y && curr.x == next.x)) {
                // 检查是否可以直接从prev到next而不经过障碍物
                if (!pathCrossesObstacles(prev, next)) {
                    continue;
                }
            }

            result.add(curr);
        }

        result.add(path.get(path.size() - 1)); // 添加终点
        return result;
    }

    /**
     * 检查路径是否穿过障碍物
     */
    private boolean pathCrossesObstacles(Point start, Point end) {
        // 如果不是水平或垂直线，则无法直接连接
        if (start.x != end.x && start.y != end.y) {
            return true;
        }

        // 检查水平线
        if (start.y == end.y) {
            int minX = Math.min(start.x, end.x);
            int maxX = Math.max(start.x, end.x);
            for (int x = minX; x <= maxX; x++) {
                Point p = new Point(x, start.y);
                for (Rectangle obstacle : obstacles) {
                    if (obstacle.contains(p)) {
                        return true;
                    }
                }
            }
        }
        // 检查垂直线
        else {
            int minY = Math.min(start.y, end.y);
            int maxY = Math.max(start.y, end.y);
            for (int y = minY; y <= maxY; y++) {
                Point p = new Point(start.x, y);
                for (Rectangle obstacle : obstacles) {
                    if (obstacle.contains(p)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 门对类，用于表示需要连接的两个门
     */
    private static class DoorPair {
        Door door1;
        Door door2;

        DoorPair(Door door1, Door door2) {
            this.door1 = door1;
            this.door2 = door2;
        }
    }

    // 获取最适合连接的 Point，需要一次从前往后尝试
    public ArrayList<Point> getBestPointForConnection(Point start, Door door) {
        HashMap<Point, Double> pointScores = new HashMap<>();
        // 最大追踪距离
        final int MAX_DISTANCE = 12;

        // 第一轮：查找与起点距离小于 10 的且不可达同 Room 的点
        for (Point point : result) {
            if (point.equals(start)) continue;

            // 计算距离
            double distance = start.distance(point);
            // 如果距离小于 10 且可以从起点到达该点，记录这个点
            int steps = canReach(start, door);
            if (distance < 15 && steps >= 0) {
                // 远离因子
                int count = 1;
                for (Door sameRoomDoor : door.getRoom().getDoors()) {
                    if (sameRoomDoor.equals(door)) continue;
                    int stepsToPoint = canReach(point, sameRoomDoor, MAX_DISTANCE);
                    if (stepsToPoint >= 0) {
                        count += (MAX_DISTANCE - stepsToPoint);
                    }
                }

                pointScores.put(point, distance * count);
            }
        }

        // 如果没有找到合适的点，尝试第二种策略
        if (pointScores.isEmpty()) {
            for (Point point : result) {
                // 计算该点到门的距离
                double distance = point.distance(door.getPoint());
                // 如果距离小于10且可以从该点到达门，记录这个点
                int stepsToPoint = canReach(point, door);
                if (distance < 15 && stepsToPoint >= 0) {
                    // 远离因子
                    int count = 1;
                    for (Door sameRoomDoor : door.getRoom().getDoors()) {
                        if (sameRoomDoor.equals(door)) continue;
                        int stepsToDoor = canReach(point, sameRoomDoor, MAX_DISTANCE);
                        if (stepsToDoor >= 0) {
                            count += (MAX_DISTANCE - stepsToPoint);
                        }
                    }
                    pointScores.put(point, start.distance(point) * count); // 仍然使用到起点的距离进行排序
                }
            }
        }

        ArrayList<Point> points = new ArrayList<>();
        // 按距离最小排序
        pointScores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> points.add(entry.getKey()));

        if (points.isEmpty()) points.add(door.getPoint());

        return points;
    }

    /*
    判断指定 Point 是否可以通过 result 中已有的 Point 连接到目标 Door，可以设置步数上限
    返回值为步数，如果不可达则返回-1
     */
    public int canReach(Point start, Door target, int limit) {
        // 如果起点就是目标点，直接返回0（表示0步可达）
        Point targetPoint = target.getPoint();
        if (start.equals(targetPoint)) {
            return 0;
        }

        // 使用BFS算法判断是否可达
        Queue<Point> queue = new LinkedList<>();
        Set<Point> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        // 四个方向的移动：上、右、下、左
        int[][] directions = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
        
        // 记录搜索步数
        int steps = 0;

        while (!queue.isEmpty() && (limit <= 0 || steps < limit)) {
            // 当前层的节点数
            int levelSize = queue.size();
            
            // 处理当前层的所有节点
            for (int i = 0; i < levelSize; i++) {
                Point current = queue.poll();

                // 检查四个方向
                for (int[] dir : directions) {
                    int newX = current.x + dir[0];
                    int newY = current.y + dir[1];
                    Point newPoint = new Point(newX, newY);

                    // 如果新点是目标点
                    if (newPoint.equals(targetPoint)) {
                        return steps + 1;  // 返回步数（当前步数+1）
                    }

                    // 如果新点在result集合中且未访问过
                    if (result.contains(newPoint) && !visited.contains(newPoint)) {
                        queue.add(newPoint);
                        visited.add(newPoint);
                    }
                }
            }
            
            // 当前层处理完毕，步数加1
            steps++;
        }

        // 如果遍历完所有可达点仍未找到目标点，或者达到了步数限制，则返回-1表示不可达
        return -1;
    }
    
    /*
    判断指定 Point 是否可以通过 result 中已有的 Point 连接到目标 Door，无步数限制
    返回值为步数，如果不可达则返回-1
     */
    public int canReach(Point start, Door target) {
        // 调用有限制参数的方法，传入0表示无限制
        return canReach(start, target, 0);
    }
}
