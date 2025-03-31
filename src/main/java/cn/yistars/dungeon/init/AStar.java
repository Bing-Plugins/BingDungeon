package cn.yistars.dungeon.init;

import java.awt.*;
import java.util.*;
import java.util.List;

public class AStar {
    private Set<Rectangle> obstacles;
    private Set<Point> existingPath;
    private final int[][] directions = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}}; // 上、右、下、左

    public AStar(Set<Rectangle> obstacles, Set<Point> existingPath) {
        this.obstacles = obstacles;
        this.existingPath = existingPath;
    }

    // A* 寻路算法
    public List<Point> findPath(Point start, Point end) {
        // 开放列表，存储待检查的节点
        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingInt(node -> node.f));
        // 关闭列表，存储已检查的节点
        Set<Point> closedList = new HashSet<>();
        // 节点的父节点映射，用于重建路径
        Map<Point, Node> parentMap = new HashMap<>();

        // 添加起始节点到开放列表
        Node startNode = new Node(start, null, 0, manhattanDistance(start, end));
        openList.add(startNode);
        parentMap.put(start, startNode);

        while (!openList.isEmpty()) {
            // 从开放列表获取f值最小的节点
            Node current = openList.poll();

            // 如果当前节点是目标节点，重建并返回路径
            if (current.point.equals(end)) {
                return reconstructPath(current, parentMap);
            }

            // 将当前节点添加到关闭列表
            closedList.add(current.point);

            // 检查四个方向的相邻节点
            for (int[] dir : directions) {
                Point nextPoint = new Point(current.point.x + dir[0], current.point.y + dir[1]);

                // 如果节点在关闭列表中或是障碍物，跳过
                if (closedList.contains(nextPoint) || isObstacle(nextPoint)) {
                    continue;
                }

                // 计算从起始点到当前相邻节点的总代价
                int g = current.g + 1;

                // 如果是已存在的路径点，降低代价以鼓励路径复用
                if (existingPath.contains(nextPoint)) {
                    g -= 0.5;
                }

                // 计算启发式函数值（曼哈顿距离）
                int h = manhattanDistance(nextPoint, end);

                // 总代价
                int f = g + h;

                // 如果相邻节点不在开放列表中，或者找到了更好的路径
                Node neighbor = parentMap.getOrDefault(nextPoint, null);
                if (neighbor == null || g < neighbor.g) {
                    neighbor = new Node(nextPoint, current, g, h);
                    openList.add(neighbor);
                    parentMap.put(nextPoint, neighbor);
                }
            }
        }

        // 如果无法找到路径，返回null
        return null;
    }

    // 检查点是否在障碍物内
    private boolean isObstacle(Point point) {
        for (Rectangle obstacle : obstacles) {
            if (obstacle.contains(point)) {
                return true;
            }
        }
        return false;
    }

    // 计算曼哈顿距离
    private int manhattanDistance(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    // 重建路径，确保所有拐弯都是直角
    private List<Point> reconstructPath(Node end, Map<Point, Node> parentMap) {
        List<Point> path = new ArrayList<>();
        Node current = end;

        // 从目标节点回溯到起始节点
        while (current != null) {
            path.add(current.point);
            current = current.parent;
        }

        // 反转路径，使其从起始节点到目标节点
        Collections.reverse(path);

        // 确保路径中的拐弯都是直角
        return optimizePathWithRightAngles(path);
    }

    // 优化路径，确保拐弯都是直角
    private List<Point> optimizePathWithRightAngles(List<Point> originalPath) {
        if (originalPath.size() <= 2) return originalPath;

        List<Point> optimizedPath = new ArrayList<>();
        optimizedPath.add(originalPath.get(0));

        for (int i = 1; i < originalPath.size() - 1; i++) {
            Point prev = originalPath.get(i - 1);
            Point current = originalPath.get(i);
            Point next = originalPath.get(i + 1);

            // 检查是否需要插入中间点以确保直角拐弯
            if (prev.x != next.x && prev.y != next.y) {
                // 创建转折点
                Point corner = new Point(prev.x, next.y);

                // 检查转折点是否为障碍物
                if (!isObstacle(corner)) {
                    optimizedPath.add(corner);
                } else {
                    // 尝试另一个转折点
                    corner = new Point(next.x, prev.y);
                    if (!isObstacle(corner)) {
                        optimizedPath.add(corner);
                    }
                }
            }

            optimizedPath.add(current);
        }

        optimizedPath.add(originalPath.get(originalPath.size() - 1));
        return optimizedPath;
    }

    // A* 节点类
    private static class Node {
        Point point;
        Node parent;
        int g; // 从起点到当前节点的代价
        int h; // 启发式函数值（估计从当前节点到目标的代价）
        int f; // 总代价 f = g + h

        public Node(Point point, Node parent, int g, int h) {
            this.point = point;
            this.parent = parent;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }
    }
}