package cn.yistars.dungeon.init;

import java.awt.*;
import java.util.*;
import java.util.List;

public class AntColonyOptimizer {
    private List<Point> doors;
    private Set<Rectangle> obstacles;
    private double[][] pheromones;
    private double[][] distances;
    private int antCount = 10; // 蚂蚁数量
    private int maxIterations = 100; // 最大迭代次数
    private double alpha = 1.0; // 信息素重要程度因子
    private double beta = 2.0; // 启发式因子
    private double evaporationRate = 0.5; // 信息素蒸发率
    private double Q = 500; // 信息素增强常数

    public AntColonyOptimizer(List<Point> doors, Set<Rectangle> obstacles) {
        this.doors = doors;
        this.obstacles = obstacles;
        int n = doors.size();
        pheromones = new double[n][n];
        distances = new double[n][n];

        // 初始化信息素和距离矩阵
        initializeMatrices();
    }

    /**
     * 初始化信息素和距离矩阵
     */
    private void initializeMatrices() {
        int n = doors.size();

        // 初始化所有信息素为1.0
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                pheromones[i][j] = 1.0;
            }
        }

        // 计算距离矩阵
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    distances[i][j] = 0;
                } else {
                    // 使用曼哈顿距离，考虑障碍物
                    distances[i][j] = calculateDistance(doors.get(i), doors.get(j));
                }
            }
        }
    }

    /**
     * 计算两点间的距离，考虑障碍物
     */
    private double calculateDistance(Point start, Point end) {
        // 使用曼哈顿距离作为基础
        double distance = Math.abs(start.x - end.x) + Math.abs(start.y - end.y);

        // 如果两点之间有障碍物，增加距离惩罚
        if (hasObstacleBetween(start, end)) {
            distance *= 1.5;
        }

        return distance;
    }

    /**
     * 检查两点之间是否有障碍物
     */
    private boolean hasObstacleBetween(Point start, Point end) {
        // 简单实现：检查直线路径上的若干点
        int steps = 10;
        for (int i = 1; i < steps; i++) {
            double ratio = (double) i / steps;
            int x = (int) (start.x + ratio * (end.x - start.x));
            int y = (int) (start.y + ratio * (end.y - start.y));
            Point point = new Point(x, y);

            for (Rectangle obstacle : obstacles) {
                if (obstacle.contains(point)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 使用蚁群算法找出最优路径顺序
     */
    public List<Point> findOptimalOrder() {
        int n = doors.size();
        List<Point> bestOrder = null;
        double bestLength = Double.MAX_VALUE;

        // 迭代指定次数
        for (int iter = 0; iter < maxIterations; iter++) {
            List<List<Integer>> antPaths = new ArrayList<>();
            double[] pathLengths = new double[antCount];

            // 每只蚂蚁构建一条路径
            for (int ant = 0; ant < antCount; ant++) {
                List<Integer> path = buildAntPath(n);
                antPaths.add(path);

                // 计算路径长度
                pathLengths[ant] = calculatePathLength(path);

                // 更新最佳路径
                if (pathLengths[ant] < bestLength) {
                    bestLength = pathLengths[ant];
                    bestOrder = new ArrayList<>();
                    for (int index : path) {
                        bestOrder.add(doors.get(index));
                    }
                }
            }

            // 更新信息素
            updatePheromones(antPaths, pathLengths);
        }

        return bestOrder;
    }

    /**
     * 构建单只蚂蚁的路径
     */
    private List<Integer> buildAntPath(int n) {
        List<Integer> path = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();

        // 随机选择起始点
        int start = new Random().nextInt(n);
        path.add(start);
        visited.add(start);

        // 构建完整路径
        while (path.size() < n) {
            int current = path.get(path.size() - 1);
            int next = selectNextNode(current, visited);
            path.add(next);
            visited.add(next);
        }

        return path;
    }

    /**
     * 选择下一个节点
     */
    private int selectNextNode(int current, Set<Integer> visited) {
        int n = doors.size();

        // 计算概率
        double[] probabilities = new double[n];
        double sum = 0.0;

        for (int i = 0; i < n; i++) {
            if (visited.contains(i)) {
                probabilities[i] = 0.0;
            } else {
                // 使用信息素和距离计算概率
                probabilities[i] = Math.pow(pheromones[current][i], alpha) *
                        Math.pow(1.0 / distances[current][i], beta);
                sum += probabilities[i];
            }
        }

        // 轮盘选择法
        double rand = new Random().nextDouble() * sum;
        double partialSum = 0.0;

        for (int i = 0; i < n; i++) {
            if (visited.contains(i)) continue;

            partialSum += probabilities[i];
            if (partialSum >= rand) {
                return i;
            }
        }

        // 如果出现意外情况，返回第一个未访问的节点
        for (int i = 0; i < n; i++) {
            if (!visited.contains(i)) {
                return i;
            }
        }

        return -1; // 不应该到达这里
    }

    /**
     * 计算路径长度
     */
    private double calculatePathLength(List<Integer> path) {
        double length = 0.0;

        for (int i = 0; i < path.size() - 1; i++) {
            length += distances[path.get(i)][path.get(i + 1)];
        }

        // 添加回到起点的距离，形成闭环
        length += distances[path.get(path.size() - 1)][path.get(0)];

        return length;
    }

    /**
     * 更新信息素
     */
    private void updatePheromones(List<List<Integer>> antPaths, double[] pathLengths) {
        int n = doors.size();

        // 信息素蒸发
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                pheromones[i][j] *= (1.0 - evaporationRate);
            }
        }

        // 信息素添加
        for (int ant = 0; ant < antCount; ant++) {
            double pheromoneToAdd = Q / pathLengths[ant];
            List<Integer> path = antPaths.get(ant);

            for (int i = 0; i < path.size() - 1; i++) {
                int from = path.get(i);
                int to = path.get(i + 1);
                pheromones[from][to] += pheromoneToAdd;
                pheromones[to][from] += pheromoneToAdd; // 双向更新
            }

            // 闭环路径的最后一条边
            int last = path.get(path.size() - 1);
            int first = path.get(0);
            pheromones[last][first] += pheromoneToAdd;
            pheromones[first][last] += pheromoneToAdd;
        }
    }
}