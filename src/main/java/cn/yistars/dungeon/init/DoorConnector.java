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
    private final AntColonyOptimization aco;
    private final Map<Room, Set<Door>> roomDoors = new HashMap<>();
    private final Map<Door, Set<Point>> doorConnections = new HashMap<>();

    public DoorConnector(ArrayList<Room> rooms) {
        this.rooms = rooms;
        this.aco = new AntColonyOptimization();

        // 添加所有障碍物
        for (Room room : rooms) {
            aco.addObstacle(room.getRectangle());
        }

        // 收集每个房间的门
        collectRoomDoors();

        // 连接所有房间
        connectRooms();
    }

    // 收集每个房间的门
    private void collectRoomDoors() {
        for (Room room : rooms) {
            roomDoors.put(room, new HashSet<>(room.getDoors()));

            // 将门的点位添加到结果中
            for (Door door : room.getDoors()) {
                result.add(door.getPoint());
                doorConnections.put(door, new HashSet<>());
            }
        }
    }

    // 连接所有房间
    private void connectRooms() {
        // 获取所有门
        List<Door> allDoors = new ArrayList<>();
        for (Room room : rooms) {
            allDoors.addAll(room.getDoors());
        }

        // 为每个门找到最佳连接
        for (int i = 0; i < allDoors.size(); i++) {
            Door door1 = allDoors.get(i);

            // 跳过已连接的门
            if (isConnected(door1)) {
                continue;
            }

            Point start = door1.getPoint();
            Door bestDoor = null;
            double bestScore = Double.MAX_VALUE;

            // 对每个其他门评估连接
            for (int j = 0; j < allDoors.size(); j++) {
                if (i == j) continue;

                Door door2 = allDoors.get(j);

                // 避免连接同一房间的门，除非没有其他选择
                if (door1.getRoom() == door2.getRoom()) {
                    // 如果是同一房间的门，需要确保它们的距离足够远
                    double distance = start.distance(door2.getPoint());
                    // 距离太近，跳过
                    if (distance < door1.getRoom().getRectangle().width / 2) {
                        continue;
                    }
                }

                // 已经连接的门，跳过
                if (isConnected(door2)) {
                    continue;
                }

                // 计算连接分数，优先连接不同房间的门
                double score = calculateConnectionScore(door1, door2);

                if (score < bestScore) {
                    bestScore = score;
                    bestDoor = door2;
                }
            }

            // 如果找到了适合连接的门，进行连接
            if (bestDoor != null) {
                connectDoors(door1, bestDoor);
            }
        }

        // 验证所有房间都已连接，如果没有，尝试连接剩余的
        ensureAllRoomsConnected();
    }

    // 检查一个门是否已连接
    private boolean isConnected(Door door) {
        return !doorConnections.get(door).isEmpty();
    }

    // 计算连接两个门的分数（值越小越好）
    private double calculateConnectionScore(Door door1, Door door2) {
        Point p1 = door1.getPoint();
        Point p2 = door2.getPoint();

        // 曼哈顿距离
        double distance = Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);

        // 如果是同一房间的门，增加分数（减少优先级）
        if (door1.getRoom() == door2.getRoom()) {
            return distance * 2;
        }

        return distance;
    }

    // 连接两个门
    private void connectDoors(Door door1, Door door2) {
        Point start = door1.getPoint();
        Point end = door2.getPoint();

        // 检查是否可以直接到达
        boolean canReachDirect = canReach(start, end);

        // 如果不能直接到达，尝试在附近寻找可到达的点
        if (!canReachDirect) {
            Point nearestPoint = findNearestReachablePoint(start, 10);

            if (nearestPoint != null) {
                // 连接到最近的可到达点
                List<Point> pathToNearest = aco.findPath(start, nearestPoint, true);
                addPathToResult(pathToNearest);

                // 更新起点
                start = nearestPoint;
                canReachDirect = canReach(start, end);
            }
        }

        // 如果还是不能直接到达，尝试在目标门附近找一个点
        if (!canReachDirect) {
            Point targetNearPoint = findPointNearTargetDoor(door2, start, 10);

            if (targetNearPoint != null) {
                // 连接到目标门附近的点
                List<Point> pathToTarget = aco.findPath(start, targetNearPoint, false);
                addPathToResult(pathToTarget);

                // 连接目标门附近的点到目标门
                List<Point> finalPath = aco.findPath(targetNearPoint, end, true);
                addPathToResult(finalPath);
            } else {
                // 直接尝试连接到目标门
                List<Point> directPath = aco.findPath(start, end, false);
                addPathToResult(directPath);
            }
        } else {
            // 如果可以直接到达，使用蚁群算法找到最佳路径
            List<Point> path = aco.findPath(start, end, true);
            addPathToResult(path);
        }

        // 记录连接
        doorConnections.get(door1).add(end);
        doorConnections.get(door2).add(start);
    }

    // 检查两点之间是否可以直接到达
    private boolean canReach(Point start, Point end) {
        // 对于水平或垂直路径，检查是否有障碍物
        if (start.x == end.x) {
            int minY = Math.min(start.y, end.y);
            int maxY = Math.max(start.y, end.y);

            for (int y = minY + 1; y < maxY; y++) {
                if (isObstacle(new Point(start.x, y))) {
                    return false;
                }
            }
            return true;
        } else if (start.y == end.y) {
            int minX = Math.min(start.x, end.x);
            int maxX = Math.max(start.x, end.x);

            for (int x = minX + 1; x < maxX; x++) {
                if (isObstacle(new Point(x, start.y))) {
                    return false;
                }
            }
            return true;
        }

        // 如果不是水平或垂直路径，则检查L形路径
        Point corner1 = new Point(start.x, end.y);
        if (!isObstacle(corner1) && canReach(start, corner1) && canReach(corner1, end)) {
            return true;
        }

        Point corner2 = new Point(end.x, start.y);
        return !isObstacle(corner2) && canReach(start, corner2) && canReach(corner2, end);
    }

    // 检查点是否是障碍物
    private boolean isObstacle(Point p) {
        for (Room room : rooms) {
            Rectangle rect = room.getRectangle();
            if (rect.contains(p)) {
                return true;
            }
        }
        return false;
    }

    // 在起点附近寻找最近的可到达点
    private Point findNearestReachablePoint(Point start, int searchRange) {
        Point nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (int dx = -searchRange; dx <= searchRange; dx++) {
            for (int dy = -searchRange; dy <= searchRange; dy++) {
                if (dx == 0 && dy == 0) continue;

                Point p = new Point(start.x + dx, start.y + dy);

                // 如果点是障碍物，跳过
                if (isObstacle(p)) {
                    continue;
                }

                // 检查是否可以到达
                if (canReach(start, p)) {
                    double distance = start.distance(p);
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearest = p;
                    }
                }
            }
        }

        return nearest;
    }

    // 在目标门附近寻找一个点
    private Point findPointNearTargetDoor(Door targetDoor, Point start, int searchRange) {
        Point end = targetDoor.getPoint();
        Point bestPoint = null;
        double bestScore = Double.MIN_VALUE;

        for (int dx = -searchRange; dx <= searchRange; dx++) {
            for (int dy = -searchRange; dy <= searchRange; dy++) {
                if (dx == 0 && dy == 0) continue;

                Point p = new Point(end.x + dx, end.y + dy);

                // 如果点是障碍物，跳过
                if (isObstacle(p)) {
                    continue;
                }

                // 计算分数：离目标远，但离起点近的点更好
                double distanceToEnd = p.distance(end);
                double distanceToStart = p.distance(start);
                double score = distanceToEnd - 0.5 * distanceToStart;

                if (score > bestScore) {
                    bestScore = score;
                    bestPoint = p;
                }
            }
        }

        return bestPoint;
    }

    // 将路径添加到结果中
    private void addPathToResult(List<Point> path) {
        if (path != null) {
            result.addAll(path);
        }
    }

    // 确保所有房间都已连接
    private void ensureAllRoomsConnected() {
        // 找出哪些房间还没有连接
        Set<Room> unconnectedRooms = new HashSet<>();

        for (Room room : rooms) {
            boolean hasConnectedDoor = false;

            for (Door door : room.getDoors()) {
                if (isConnected(door)) {
                    hasConnectedDoor = true;
                    break;
                }
            }

            if (!hasConnectedDoor) {
                unconnectedRooms.add(room);
            }
        }

        // 连接未连接的房间
        for (Room room : unconnectedRooms) {
            connectUnconnectedRoom(room);
        }
    }

    // 连接未连接的房间
    private void connectUnconnectedRoom(Room room) {
        // 选择一个未连接的门
        Door unconnectedDoor = null;
        for (Door door : room.getDoors()) {
            if (!isConnected(door)) {
                unconnectedDoor = door;
                break;
            }
        }

        if (unconnectedDoor == null) return;

        // 寻找最近的已连接门
        Door nearestConnectedDoor = null;
        double minDistance = Double.MAX_VALUE;

        for (Room otherRoom : rooms) {
            for (Door door : otherRoom.getDoors()) {
                if (isConnected(door)) {
                    double distance = unconnectedDoor.getPoint().distance(door.getPoint());
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearestConnectedDoor = door;
                    }
                }
            }
        }

        // 如果找到了最近的已连接门，连接它们
        if (nearestConnectedDoor != null) {
            connectDoors(unconnectedDoor, nearestConnectedDoor);
        }
    }
}