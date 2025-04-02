package cn.yistars.dungeon.init;

import java.awt.*;
import java.util.*;
import java.util.List;

public class AntColonyOptimization {
    private final int antCount; // 蚂蚁数量
    private final int maxIterations; // 最大迭代次数
    private final double alpha; // 信息素重要程度
    private final double beta; // 启发式因子重要程度
    private final double evaporationRate; // 信息素蒸发率
    private final double q; // 信息素增加强度
    private final Random random = new Random();

    // 存储障碍物信息
    private final Set<Point> obstacles = new HashSet<>();

    // 存储节点间的信息素浓度
    private Map<Edge, Double> pheromones = new HashMap<>();

    // 表示图中的边
    private static class Edge {
        final Point from;
        final Point to;

        Edge(Point from, Point to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge edge = (Edge) o;
            return (from.equals(edge.from) && to.equals(edge.to)) ||
                    (from.equals(edge.to) && to.equals(edge.from));
        }

        @Override
        public int hashCode() {
            return from.hashCode() + to.hashCode();
        }
    }

    public AntColonyOptimization() {
        this(50, 100, 1.0, 2.0, 0.5, 100.0);
    }

    public AntColonyOptimization(int antCount, int maxIterations, double alpha,
                                 double beta, double evaporationRate, double q) {
        this.antCount = antCount;
        this.maxIterations = maxIterations;
        this.alpha = alpha;
        this.beta = beta;
        this.evaporationRate = evaporationRate;
        this.q = q;
    }

    public void addObstacle(Rectangle rect) {
        for (int x = rect.x; x < rect.x + rect.width; x++) {
            for (int y = rect.y; y < rect.y + rect.height; y++) {
                obstacles.add(new Point(x, y));
            }
        }
    }

    // 曼哈顿距离
    private int manhattanDistance(Point p1, Point p2) {
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    }

    // 检查点是否有效（不在障碍物上）
    private boolean isValidPoint(Point p) {
        return !obstacles.contains(p);
    }

    // 使用蚁群算法找到两点间的路径
    public List<Point> findPath(Point start, Point end, boolean canReachDirect) {
        // 如果可以直接到达并且路径有效，则直接返回
        if (canReachDirect && isValidStraightPath(start, end)) {
            return createStraightPath(start, end);
        }

        // 初始化信息素
        initializePheromones();

        // 用于存储最佳路径
        List<Point> bestPath = null;
        int bestPathLength = Integer.MAX_VALUE;

        // 迭代
        for (int i = 0; i < maxIterations; i++) {
            List<List<Point>> antPaths = new ArrayList<>();

            // 每只蚂蚁寻找路径
            for (int ant = 0; ant < antCount; ant++) {
                List<Point> path = constructPath(start, end);
                if (path != null) {
                    antPaths.add(path);

                    // 更新最佳路径
                    if (path.size() < bestPathLength) {
                        bestPathLength = path.size();
                        bestPath = new ArrayList<>(path);
                    }
                }
            }

            // 更新信息素
            updatePheromones(antPaths);
        }

        return bestPath != null ? bestPath : createLShapedPath(start, end);
    }

    // 如果没有找到路径，则创建一个L形路径
    private List<Point> createLShapedPath(Point start, Point end) {
        List<Point> path = new ArrayList<>();
        Point corner = new Point(start.x, end.y);

        // 如果L形拐角处于障碍物位置，尝试另一个方向的L形路径
        if (obstacles.contains(corner)) {
            corner = new Point(end.x, start.y);
        }

        // 如果两个方向的L形路径拐角都在障碍物上，则找一条绕开障碍物的路径
        if (obstacles.contains(corner)) {
            return findAlternativePath(start, end);
        }

        path.add(start);
        path.addAll(createStraightPath(start, corner).subList(1, createStraightPath(start, corner).size()));
        path.addAll(createStraightPath(corner, end).subList(1, createStraightPath(corner, end).size()));

        return path;
    }

    // 寻找替代路径，绕开障碍物
    private List<Point> findAlternativePath(Point start, Point end) {
        // 尝试寻找一个可行的中间点
        for (int offset = 1; offset < 20; offset++) {
            Point[] possibleCorners = {
                    new Point(start.x + offset, start.y),
                    new Point(start.x - offset, start.y),
                    new Point(start.x, start.y + offset),
                    new Point(start.x, start.y - offset)
            };

            for (Point c1 : possibleCorners) {
                if (isValidPoint(c1)) {
                    Point[] secondCorners = {
                            new Point(c1.x, end.y),
                            new Point(end.x, c1.y)
                    };

                    for (Point c2 : secondCorners) {
                        if (isValidPoint(c2) &&
                                isValidStraightPath(start, c1) &&
                                isValidStraightPath(c1, c2) &&
                                isValidStraightPath(c2, end)) {

                            List<Point> path = new ArrayList<>();
                            path.add(start);
                            path.addAll(createStraightPath(start, c1).subList(1, createStraightPath(start, c1).size()));
                            path.addAll(createStraightPath(c1, c2).subList(1, createStraightPath(c1, c2).size()));
                            path.addAll(createStraightPath(c2, end).subList(1, createStraightPath(c2, end).size()));
                            return path;
                        }
                    }
                }
            }
        }

        // 如果所有尝试都失败，返回空列表
        return new ArrayList<>();
    }

    // 检查两点间的直线路径是否有效（不穿过障碍物）
    private boolean isValidStraightPath(Point start, Point end) {
        // 对于水平或垂直路径
        if (start.x == end.x || start.y == end.y) {
            if (start.x == end.x) {
                int minY = Math.min(start.y, end.y);
                int maxY = Math.max(start.y, end.y);

                for (int y = minY + 1; y < maxY; y++) {
                    if (obstacles.contains(new Point(start.x, y))) {
                        return false;
                    }
                }
            } else {
                int minX = Math.min(start.x, end.x);
                int maxX = Math.max(start.x, end.x);

                for (int x = minX + 1; x < maxX; x++) {
                    if (obstacles.contains(new Point(x, start.y))) {
                        return false;
                    }
                }
            }
            return true;
        }

        return false;
    }

    // 创建两点间的直线路径
    private List<Point> createStraightPath(Point start, Point end) {
        List<Point> path = new ArrayList<>();
        path.add(start);

        // 对于水平路径
        if (start.y == end.y) {
            int direction = start.x < end.x ? 1 : -1;
            for (int x = start.x + direction; x != end.x + direction; x += direction) {
                path.add(new Point(x, start.y));
            }
        }
        // 对于垂直路径
        else if (start.x == end.x) {
            int direction = start.y < end.y ? 1 : -1;
            for (int y = start.y + direction; y != end.y + direction; y += direction) {
                path.add(new Point(start.x, y));
            }
        }

        return path;
    }

    // 初始化信息素
    private void initializePheromones() {
        pheromones = new HashMap<>();
    }

    // 蚂蚁构建路径
    private List<Point> constructPath(Point start, Point end) {
        Set<Point> visited = new HashSet<>();
        List<Point> path = new ArrayList<>();
        path.add(start);
        visited.add(start);

        Point current = start;

        while (!current.equals(end) && path.size() < 100) {
            List<Point> candidates = getNeighbors(current);
            candidates.removeIf(visited::contains);

            if (candidates.isEmpty()) {
                return null; // 无法找到有效路径
            }

            // 计算概率
            Map<Point, Double> probabilities = new HashMap<>();
            double totalProbability = 0;

            for (Point candidate : candidates) {
                Edge edge = new Edge(current, candidate);
                double pheromone = pheromones.getOrDefault(edge, 1.0);
                double distance = 1.0 / manhattanDistance(candidate, end);
                double probability = Math.pow(pheromone, alpha) * Math.pow(distance, beta);
                probabilities.put(candidate, probability);
                totalProbability += probability;
            }

            // 选择下一个点
            double random = this.random.nextDouble() * totalProbability;
            double cumulativeProbability = 0;
            Point next = null;

            for (Map.Entry<Point, Double> entry : probabilities.entrySet()) {
                cumulativeProbability += entry.getValue();
                if (cumulativeProbability >= random) {
                    next = entry.getKey();
                    break;
                }
            }

            if (next == null) {
                next = candidates.get(this.random.nextInt(candidates.size()));
            }

            path.add(next);
            visited.add(next);
            current = next;
        }

        if (!current.equals(end)) {
            return null; // 无法到达目标
        }

        return path;
    }

    // 获取有效的邻居点（确保路径只包含直角拐弯）
    private List<Point> getNeighbors(Point p) {
        List<Point> neighbors = new ArrayList<>();

        Point[] possibleNeighbors = {
                new Point(p.x + 1, p.y),
                new Point(p.x - 1, p.y),
                new Point(p.x, p.y + 1),
                new Point(p.x, p.y - 1)
        };

        for (Point neighbor : possibleNeighbors) {
            if (isValidPoint(neighbor)) {
                neighbors.add(neighbor);
            }
        }

        return neighbors;
    }

    // 更新信息素
    private void updatePheromones(List<List<Point>> antPaths) {
        // 信息素蒸发
        for (Edge edge : pheromones.keySet()) {
            pheromones.put(edge, pheromones.get(edge) * (1 - evaporationRate));
        }

        // 增加信息素
        for (List<Point> path : antPaths) {
            double pheromoneToAdd = q / path.size();

            for (int i = 0; i < path.size() - 1; i++) {
                Edge edge = new Edge(path.get(i), path.get(i + 1));
                pheromones.put(edge, pheromones.getOrDefault(edge, 0.0) + pheromoneToAdd);
            }
        }
    }
}