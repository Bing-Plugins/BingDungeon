package cn.yistars.dungeon.init;

import java.awt.*;
import java.util.*;
import java.util.List;

public class AStarPathFinder {
    private final Set<Rectangle> obstacles;
    private static final int[][] DIRECTIONS = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}}; // 上、右、下、左

    public AStarPathFinder(Set<Rectangle> obstacles) {
        this.obstacles = obstacles;
    }

    /**
     * 使用A*算法寻找从起点到终点的路径
     */
    public List<Point> findPath(Point start, Point end) {
        // 初始化开放列表和关闭列表
        PriorityQueue<Node> openList = new PriorityQueue<>();
        Set<Point> closedList = new HashSet<>();
        Map<Point, Node> nodeMap = new HashMap<>();

        // 添加起始节点
        Node startNode = new Node(start, null, 0, calculateHeuristic(start, end));
        openList.add(startNode);
        nodeMap.put(start, startNode);

        while (!openList.isEmpty()) {
            // 获取估值最小的节点
            Node current = openList.poll();

            // 如果找到终点，构建路径并返回
            if (current.position.equals(end)) {
                return buildPath(current);
            }

            // 将当前节点加入关闭列表
            closedList.add(current.position);

            // 探索四个方向的邻居
            for (int[] direction : DIRECTIONS) {
                Point nextPoint = new Point(current.position.x + direction[0], current.position.y + direction[1]);

                // 如果点在关闭列表中，跳过
                if (closedList.contains(nextPoint)) {
                    continue;
                }

                // 如果点在障碍物中，跳过
                if (isPointInObstacle(nextPoint)) {
                    continue;
                }

                // 计算新的g值（从起点到当前点的实际代价）
                double newG = current.g + 1;

                // 检查是否已存在更优的路径
                Node neighborNode = nodeMap.get(nextPoint);
                if (neighborNode == null || newG < neighborNode.g) {
                    // 创建或更新节点
                    double h = calculateHeuristic(nextPoint, end);
                    Node newNode = new Node(nextPoint, current, newG, h);

                    if (neighborNode != null) {
                        openList.remove(neighborNode);
                    }

                    openList.add(newNode);
                    nodeMap.put(nextPoint, newNode);
                }
            }
        }

        // 如果无法找到路径，返回空列表
        return Collections.emptyList();
    }

    /**
     * 检查点是否在任何障碍物中
     */
    private boolean isPointInObstacle(Point point) {
        for (Rectangle obstacle : obstacles) {
            if (obstacle.contains(point)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 计算启发式值（曼哈顿距离）
     */
    private double calculateHeuristic(Point current, Point end) {
        return Math.abs(current.x - end.x) + Math.abs(current.y - end.y);
    }

    /**
     * 从目标节点回溯构建路径
     */
    private List<Point> buildPath(Node target) {
        List<Point> path = new ArrayList<>();
        Node current = target;

        // 从目标节点回溯到起点
        while (current != null) {
            path.add(0, current.position);
            current = current.parent;
        }

        // 返回路径（不包括起点和终点）
        if (path.size() > 2) {
            return path.subList(1, path.size() - 1);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * A*算法中的节点类
     */
    private static class Node implements Comparable<Node> {
        Point position; // 节点位置
        Node parent;    // 父节点
        double g;       // 从起点到这个节点的实际代价
        double h;       // 从这个节点到终点的估计代价
        double f;       // f = g + h

        public Node(Point position, Node parent, double g, double h) {
            this.position = position;
            this.parent = parent;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.f, other.f);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Node node = (Node) obj;
            return position.equals(node.position);
        }

        @Override
        public int hashCode() {
            return position.hashCode();
        }
    }
}