package cn.yistars.dungeon.init;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ACOPathFinder {
    private final Set<Rectangle> obstacles;
    private static final int[][] DIRECTIONS = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}}; // 上、右、下、左

    // ACO算法参数
    private static final int MAX_ITERATIONS = 100;     // 最大迭代次数
    private static final int ANT_COUNT = 50;           // 蚂蚁数量
    private static final double EVAPORATION_RATE = 0.5; // 信息素蒸发率
    private static final double ALPHA = 1.0;          // 信息素重要程度
    private static final double BETA = 2.0;           // 启发式因子重要程度
    private static final double INITIAL_PHEROMONE = 0.1; // 初始信息素

    // 地图边界（会根据起点和终点动态调整）
    private int minX, maxX, minY, maxY;

    public ACOPathFinder(Set<Rectangle> obstacles) {
        this.obstacles = obstacles;
    }

    /**
     * 使用蚁群优化算法寻找从起点到终点的路径
     */
    public List<Point> findPath(Point start, Point end) {
        // 确定搜索范围
        determineSearchBoundary(start, end);

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;

        // 初始化信息素矩阵
        double[][] pheromones = new double[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pheromones[x][y] = INITIAL_PHEROMONE;
            }
        }

        List<Point> bestPath = null;
        double bestPathLength = Double.MAX_VALUE;

        // 主循环
        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            List<List<Point>> antPaths = new ArrayList<>();

            // 每只蚂蚁构建路径
            for (int ant = 0; ant < ANT_COUNT; ant++) {
                List<Point> path = buildAntPath(start, end, pheromones);

                // 如果找到了有效路径
                if (!path.isEmpty()) {
                    antPaths.add(path);

                    // 更新最佳路径
                    double pathLength = path.size();
                    if (pathLength < bestPathLength) {
                        bestPathLength = pathLength;
                        bestPath = new ArrayList<>(path);
                    }
                }
            }

            // 更新信息素
            updatePheromones(pheromones, antPaths);
        }

        // 返回最佳路径（不包括起点和终点）
        if (bestPath != null && bestPath.size() > 2) {
            return bestPath.subList(1, bestPath.size() - 1);
        }
        return Collections.emptyList();
    }

    /**
     * 确定搜索边界
     */
    private void determineSearchBoundary(Point start, Point end) {
        // 基于起点和终点确定初始搜索范围
        minX = Math.min(start.x, end.x) - 20;
        maxX = Math.max(start.x, end.x) + 20;
        minY = Math.min(start.y, end.y) - 20;
        maxY = Math.max(start.y, end.y) + 20;

        // 确保搜索范围不会太小
        minX = Math.min(minX, Math.min(start.x, end.x) - Math.abs(start.x - end.x));
        maxX = Math.max(maxX, Math.max(start.x, end.x) + Math.abs(start.x - end.x));
        minY = Math.min(minY, Math.min(start.y, end.y) - Math.abs(start.y - end.y));
        maxY = Math.max(maxY, Math.max(start.y, end.y) + Math.abs(start.y - end.y));
    }

    /**
     * 构建单只蚂蚁的路径
     */
    private List<Point> buildAntPath(Point start, Point end, double[][] pheromones) {
        Set<Point> visited = new HashSet<>();
        List<Point> path = new ArrayList<>();
        path.add(start);
        visited.add(start);

        Point current = start;

        // 最大步数限制，防止无限循环
        int maxSteps = (maxX - minX + maxY - minY) * 3;
        int steps = 0;

        while (!current.equals(end) && steps < maxSteps) {
            List<Point> candidates = new ArrayList<>();
            List<Double> probabilities = new ArrayList<>();
            double totalProbability = 0.0;

            // 考虑四个方向
            for (int[] dir : DIRECTIONS) {
                Point next = new Point(current.x + dir[0], current.y + dir[1]);

                // 检查是否在边界内
                if (next.x < minX || next.x > maxX || next.y < minY || next.y > maxY) {
                    continue;
                }

                // 检查是否已访问或在障碍物中
                if (visited.contains(next) || isPointInObstacle(next)) {
                    continue;
                }

                // 获取信息素值
                double pheromone = pheromones[next.x - minX][next.y - minY];

                // 计算启发式值（与终点的曼哈顿距离的倒数）
                double heuristic = 1.0 / (Math.abs(next.x - end.x) + Math.abs(next.y - end.y) + 0.1);

                // 计算转移概率
                double probability = Math.pow(pheromone, ALPHA) * Math.pow(heuristic, BETA);

                candidates.add(next);
                probabilities.add(probability);
                totalProbability += probability;
            }

            // 如果没有可行的下一步，终止
            if (candidates.isEmpty()) {
                break;
            }

            // 根据概率选择下一步
            double rand = Math.random() * totalProbability;
            double cumulativeProbability = 0.0;
            int selectedIndex = 0;

            for (int i = 0; i < probabilities.size(); i++) {
                cumulativeProbability += probabilities.get(i);
                if (cumulativeProbability >= rand) {
                    selectedIndex = i;
                    break;
                }
            }

            Point next = candidates.get(selectedIndex);
            path.add(next);
            visited.add(next);
            current = next;
            steps++;
        }

        // 如果找到终点，返回路径
        if (current.equals(end)) {
            return path;
        }

        // 否则返回空路径
        return Collections.emptyList();
    }

    /**
     * 更新信息素矩阵
     */
    private void updatePheromones(double[][] pheromones, List<List<Point>> antPaths) {
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;

        // 信息素蒸发
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pheromones[x][y] *= (1 - EVAPORATION_RATE);
            }
        }

        // 蚂蚁释放信息素
        for (List<Point> path : antPaths) {
            // 信息素增量与路径长度成反比
            double pheromoneIncrement = 1.0 / path.size();

            for (Point p : path) {
                int x = p.x - minX;
                int y = p.y - minY;

                // 确保坐标在范围内
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    pheromones[x][y] += pheromoneIncrement;
                }
            }
        }
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
}
