package cn.yistars.dungeon.init;

import java.awt.Point;
import java.util.*;

public class AStarPathfinder {
    private final Connector connector;

    public AStarPathfinder(Connector connector) {
        this.connector = connector;
    }

    // 使用A*算法寻找从起点到终点的路径
    public List<Point> findPath(Point start, Point end) {
        // 开始直接连接尝试
        if (connector.canReach(start, end)) {
            return generateStraightPath(start, end);
        }

        // 尝试找到起点附近的可达点
        List<Point> nearPoints = connector.findReachablePointsNear(start, 5);
        for (Point near : nearPoints) {
            if (connector.canReach(near, end)) {
                List<Point> path = new ArrayList<>();
                path.addAll(generateStraightPath(start, near));
                path.addAll(generateStraightPath(near, end));
                return path;
            }
        }

        // 如果上述方法失败，使用A*寻找带有直角拐弯的路径
        return aStar(start, end);
    }

    // 生成直线路径
    private List<Point> generateStraightPath(Point start, Point end) {
        List<Point> path = new ArrayList<>();

        // 垂直线
        if (start.x == end.x) {
            int step = start.y < end.y ? 1 : -1;
            for (int y = start.y; y != end.y; y += step) {
                path.add(new Point(start.x, y));
            }
        }
        // 水平线
        else if (start.y == end.y) {
            int step = start.x < end.x ? 1 : -1;
            for (int x = start.x; x != end.x; x += step) {
                path.add(new Point(x, start.y));
            }
        }
        // 带一个拐点的直角路径
        else {
            // 先水平后垂直
            Point bend = new Point(end.x, start.y);
            path.addAll(generateStraightPath(start, bend));
            path.addAll(generateStraightPath(bend, end));
        }

        // 添加终点
        path.add(new Point(end.x, end.y));
        return path;
    }

    // A*算法实现
    private List<Point> aStar(Point start, Point end) {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<Point> closedSet = new HashSet<>();
        Map<Point, Node> allNodes = new HashMap<>();

        Node startNode = new Node(start);
        startNode.gScore = 0;
        startNode.fScore = heuristic(start, end);
        openSet.add(startNode);
        allNodes.put(start, startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.point.equals(end)) {
                return reconstructPath(current);
            }

            closedSet.add(current.point);

            // 获取可能的直角移动方向
            List<Point> neighbors = getNeighbors(current.point);

            for (Point neighbor : neighbors) {
                if (closedSet.contains(neighbor) || connector.isPointInObstacle(neighbor)) {
                    continue;
                }

                // 只允许直角拐弯
                if (current.parent != null) {
                    if (!isRightAngleTurn(current.parent.point, current.point, neighbor)) {
                        continue;
                    }
                }

                // 计算从起点到neighbor的距离
                int tentativeGScore = current.gScore + 1;

                Node neighborNode = allNodes.getOrDefault(neighbor, new Node(neighbor));

                if (!allNodes.containsKey(neighbor) || tentativeGScore < neighborNode.gScore) {
                    neighborNode.parent = current;
                    neighborNode.gScore = tentativeGScore;
                    neighborNode.fScore = tentativeGScore + heuristic(neighbor, end);

                    allNodes.put(neighbor, neighborNode);

                    if (!openSet.contains(neighborNode)) {
                        openSet.add(neighborNode);
                    }
                }
            }
        }

        return null; // 没有找到路径
    }

    // 获取相邻的点（上下左右）
    private List<Point> getNeighbors(Point p) {
        List<Point> neighbors = new ArrayList<>();
        neighbors.add(new Point(p.x + 1, p.y));  // 右
        neighbors.add(new Point(p.x - 1, p.y));  // 左
        neighbors.add(new Point(p.x, p.y + 1));  // 下
        neighbors.add(new Point(p.x, p.y - 1));  // 上
        return neighbors;
    }

    // 检查是否为直角转弯
    private boolean isRightAngleTurn(Point p1, Point p2, Point p3) {
        // 直线移动不算转弯
        if ((p1.x == p2.x && p2.x == p3.x) || (p1.y == p2.y && p2.y == p3.y)) {
            return true;
        }

        // 直角转弯：x或y坐标与前一点相同，而另一个坐标与后一点相同
        return (p1.x == p2.x && p2.y == p3.y) || (p1.y == p2.y && p2.x == p3.x);
    }

    // 启发式函数：曼哈顿距离
    private int heuristic(Point a, Point b) {
        return Connector.manhattanDistance(a, b);
    }

    // 重建路径
    private List<Point> reconstructPath(Node endNode) {
        List<Point> path = new ArrayList<>();
        Node current = endNode;

        while (current != null) {
            path.add(0, current.point);
            current = current.parent;
        }

        return path;
    }

    // A*算法的节点类
    private static class Node implements Comparable<Node> {
        Point point;
        Node parent;
        int gScore = Integer.MAX_VALUE; // 从起点到当前点的成本
        int fScore = Integer.MAX_VALUE; // gScore + 启发式估计

        public Node(Point point) {
            this.point = point;
        }

        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.fScore, other.fScore);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Node node = (Node) obj;
            return point.equals(node.point);
        }

        @Override
        public int hashCode() {
            return point.hashCode();
        }
    }
}