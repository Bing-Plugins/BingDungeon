package cn.yistars.dungeon.init;

import cn.yistars.dungeon.room.Room;
import cn.yistars.dungeon.room.door.Door;
import lombok.Getter;

import java.awt.*;
import java.util.*;
import java.util.List;

@Getter
public class Connector {
    private final ArrayList<Room> rooms;
    private final HashSet<Point> result = new HashSet<>(); // 结果
    private final HashMap<Point, Double> pheromoneMap = new HashMap<>(); // 信息素地图
    private final AntColonyOptimization aco;
    private final AStarPathfinder astar;

    public Connector(ArrayList<Room> rooms) {
        this.rooms = rooms;
        this.aco = new AntColonyOptimization(this);
        this.astar = new AStarPathfinder(this);

        // 初始化信息素地图
        initializePheromoneMap();

        // 执行路径连接
        connectAllRooms();
    }

    // 初始化信息素地图
    private void initializePheromoneMap() {
        for (Room room : rooms) {
            for (Door door : room.getDoors()) {
                pheromoneMap.put(door.getPoint(), 1.0); // 初始信息素值
            }
        }
    }

    // 连接所有房间
    private void connectAllRooms() {
        // 获取所有门
        List<Door> allDoors = getAllDoors();

        // 使用蚁群算法寻找最优的门连接方案
        List<DoorPair> doorConnections = aco.findOptimalConnections(allDoors);

        // 为每对连接的门使用A*算法创建路径
        for (DoorPair pair : doorConnections) {
            List<Point> path = astar.findPath(pair.getDoor1().getPoint(), pair.getDoor2().getPoint());
            if (path != null) {
                result.addAll(path);
            }
        }

        // 添加所有门的位置到结果中
        for (Door door : allDoors) {
            result.add(door.getPoint());
        }
    }

    // 获取所有门
    private List<Door> getAllDoors() {
        List<Door> allDoors = new ArrayList<>();
        for (Room room : rooms) {
            allDoors.addAll(room.getDoors());
        }
        return allDoors;
    }

    // 检查点是否在障碍物内
    public boolean isPointInObstacle(Point point) {
        for (Room room : rooms) {
            if (room.getRectangle().contains(point)) {
                return true;
            }
        }
        return false;
    }

    // 检查点是否可达
    public boolean canReach(Point start, Point end) {
        // 检查两点是否在同一条直线上
        if (start.x == end.x || start.y == end.y) {
            // 垂直线
            if (start.x == end.x) {
                int minY = Math.min(start.y, end.y);
                int maxY = Math.max(start.y, end.y);
                for (int y = minY; y <= maxY; y++) {
                    Point p = new Point(start.x, y);
                    if (isPointInObstacle(p) && !isDoorPoint(p)) {
                        return false;
                    }
                }
            }
            // 水平线
            else {
                int minX = Math.min(start.x, end.x);
                int maxX = Math.max(start.x, end.x);
                for (int x = minX; x <= maxX; x++) {
                    Point p = new Point(x, start.y);
                    if (isPointInObstacle(p) && !isDoorPoint(p)) {
                        return false;
                    }
                }
            }
            return true;
        }

        return false;
    }

    // 检查点是否是门
    private boolean isDoorPoint(Point point) {
        for (Room room : rooms) {
            for (Door door : room.getDoors()) {
                if (door.getPoint().equals(point)) {
                    return true;
                }
            }
        }
        return false;
    }

    // 找到点附近的可达点
    public List<Point> findReachablePointsNear(Point source, int radius) {
        List<Point> reachablePoints = new ArrayList<>();

        for (int x = source.x - radius; x <= source.x + radius; x++) {
            for (int y = source.y - radius; y <= source.y + radius; y++) {
                Point p = new Point(x, y);
                if (!isPointInObstacle(p) && canReach(source, p)) {
                    reachablePoints.add(p);
                }
            }
        }

        return reachablePoints;
    }

    // 计算两点之间的曼哈顿距离
    public static int manhattanDistance(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }
}