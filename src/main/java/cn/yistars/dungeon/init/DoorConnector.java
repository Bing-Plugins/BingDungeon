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
            connectedRooms.add(rooms.get(0));
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
        Point end = door2.getPoint();

        // 将开始和结束点添加到结果中
        result.add(start);
        result.add(end);

        // 先尝试使用A*算法
        List<Point> path = aStarPathFinder.findPath(start, end);

        // 如果A*找不到路径或者路径较差，尝试蚁群算法
        if (path == null || path.isEmpty() || pathQualityBelowThreshold(path)) {
            path = acoPathFinder.findPath(start, end);
        }

        // 简化路径，去掉不必要的拐点和交叉点
        path = simplifyPath(path);
        path = removePathCrossings(path);

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

        // 使用动态规划方法寻找最优折线路径
        for (int i = 1; i < path.size() - 1; i++) {
            Point prev = result.get(result.size() - 1); // 取最新添加的点作为前一点
            Point curr = path.get(i);
            Point next = path.get(i+1);

            // 跳过冗余点 - 检测田字型路径
            if ((prev.x == curr.x && curr.y == next.y) || 
                (prev.y == curr.y && curr.x == next.x)) {
                // 检查是否可以直接从prev到next而不经过障碍物
                if (!pathCrossesObstacles(prev, next)) {
                    continue; // 跳过当前点，形成直线或L型
                }
            }
            
            // 尝试更聪明的路径简化 - 看能否跳过多个点
            boolean canSkip = false;
            for (int j = i + 1; j < path.size(); j++) {
                Point futurePoint = path.get(j);
                // 检查是否可以直接从prev到futurePoint而不经过障碍物
                if (!pathCrossesObstacles(prev, futurePoint)) {
                    // 如果这会形成一个更好的L型，就采用
                    if ((prev.x == futurePoint.x || prev.y == futurePoint.y) || 
                        (!pathCrossesObstacles(prev, new Point(prev.x, futurePoint.y)) && 
                         !pathCrossesObstacles(new Point(prev.x, futurePoint.y), futurePoint))) {
                        // 我们可以直接跳到futurePoint
                        i = j - 1; // 调整循环变量以跳过中间点
                        canSkip = true;
                        curr = futurePoint; // 更新当前点
                        break;
                    }
                }
            }

            // 如果无法优化，则添加当前点
            if (!canSkip) {
                result.add(curr);
            } else if (i == path.size() - 2) { // 如果我们跳过了所有剩余点
                result.add(curr);
            }
        }

        result.add(path.get(path.size() - 1)); // 添加终点
        return result;
    }

    /**
     * 检测并移除路径中的交叉点
     */
    private List<Point> removePathCrossings(List<Point> path) {
        if (path == null || path.size() <= 3) return path;
        
        List<Point> result = new ArrayList<>(path);
        boolean pathChanged;
        
        do {
            pathChanged = false;
            
            // 检查路径中的每一对线段是否相交
            for (int i = 0; i < result.size() - 3; i++) {
                for (int j = i + 2; j < result.size() - 1; j++) {
                    // 检查线段 (i,i+1) 和 (j,j+1) 是否相交
                    if (linesIntersect(result.get(i), result.get(i+1), 
                                      result.get(j), result.get(j+1))) {
                        
                        // 如果相交，重新排列路径以消除交叉
                        List<Point> newPath = new ArrayList<>();
                        for (int k = 0; k <= i; k++) {
                            newPath.add(result.get(k));
                        }
                        // 反转中间路径
                        for (int k = j; k > i; k--) {
                            newPath.add(result.get(k));
                        }
                        for (int k = j+1; k < result.size(); k++) {
                            newPath.add(result.get(k));
                        }
                        
                        result = newPath;
                        pathChanged = true;
                        break;
                    }
                }
                if (pathChanged) break;
            }
        } while (pathChanged);
        
        return result;
    }

    /**
     * 检查两条线段是否相交
     * 线段1: (p1, p2), 线段2: (p3, p4)
     */
    private boolean linesIntersect(Point p1, Point p2, Point p3, Point p4) {
        // 只考虑水平和垂直线段
        if (p1.x == p2.x && p3.y == p4.y) { // 第一条是垂直线，第二条是水平线
            return p1.x >= Math.min(p3.x, p4.x) && p1.x <= Math.max(p3.x, p4.x) &&
                   p3.y >= Math.min(p1.y, p2.y) && p3.y <= Math.max(p1.y, p2.y);
        }
        
        if (p1.y == p2.y && p3.x == p4.x) { // 第一条是水平线，第二条是垂直线
            return p1.y >= Math.min(p3.y, p4.y) && p1.y <= Math.max(p3.y, p4.y) &&
                   p3.x >= Math.min(p1.x, p2.x) && p3.x <= Math.max(p1.x, p2.x);
        }
        
        return false; // 其他情况（如两条平行线）不相交
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
}
