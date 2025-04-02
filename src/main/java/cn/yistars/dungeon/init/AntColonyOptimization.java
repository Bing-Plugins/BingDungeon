package cn.yistars.dungeon.init;

import cn.yistars.dungeon.room.door.Door;

import java.awt.Point;
import java.util.*;

public class AntColonyOptimization {
    private final Connector connector;
    private static final double ALPHA = 1.0; // 信息素重要程度
    private static final double BETA = 2.0; // 启发式信息重要程度
    private static final double RHO = 0.5; // 信息素蒸发率
    private static final double Q = 100.0; // 信息素增加常数
    private static final int MAX_ITERATIONS = 100; // 最大迭代次数
    private static final int ANT_COUNT = 20; // 蚂蚁数量

    public AntColonyOptimization(Connector connector) {
        this.connector = connector;
    }

    // 寻找最优的门连接方案
    public List<DoorPair> findOptimalConnections(List<Door> allDoors) {
        HashMap<DoorPair, Double> doorPairPheromones = initializeDoorPairPheromones(allDoors);
        List<DoorPair> bestSolution = null;
        double bestSolutionQuality = Double.MAX_VALUE;

        // ACO主循环
        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            List<List<DoorPair>> antSolutions = new ArrayList<>();

            // 每只蚂蚁构建一个解决方案
            for (int ant = 0; ant < ANT_COUNT; ant++) {
                List<DoorPair> solution = buildSolution(allDoors, doorPairPheromones);
                antSolutions.add(solution);

                // 评估解决方案质量
                double solutionQuality = evaluateSolution(solution);
                if (solutionQuality < bestSolutionQuality) {
                    bestSolutionQuality = solutionQuality;
                    bestSolution = new ArrayList<>(solution);
                }
            }

            // 更新信息素
            updatePheromones(doorPairPheromones, antSolutions);
        }

        return bestSolution;
    }

    // 初始化门对之间的信息素
    private HashMap<DoorPair, Double> initializeDoorPairPheromones(List<Door> allDoors) {
        HashMap<DoorPair, Double> pheromones = new HashMap<>();

        for (int i = 0; i < allDoors.size(); i++) {
            for (int j = i + 1; j < allDoors.size(); j++) {
                Door door1 = allDoors.get(i);
                Door door2 = allDoors.get(j);

                // 同一个房间的门尽量不连接，但不完全排除
                double initialPheromone = (door1.getRoom() == door2.getRoom()) ? 0.1 : 1.0;
                pheromones.put(new DoorPair(door1, door2), initialPheromone);
            }
        }

        return pheromones;
    }

    // 构建解决方案
    private List<DoorPair> buildSolution(List<Door> allDoors, HashMap<DoorPair, Double> pheromones) {
        List<DoorPair> solution = new ArrayList<>();
        Set<Door> connectedDoors = new HashSet<>();

        // 从随机门开始
        Random random = new Random();
        Door currentDoor = allDoors.get(random.nextInt(allDoors.size()));
        connectedDoors.add(currentDoor);

        // 构建最小生成树以确保所有门都连接
        while (connectedDoors.size() < allDoors.size()) {
            Door nextDoor = selectNextDoor(currentDoor, allDoors, connectedDoors, pheromones);
            solution.add(new DoorPair(currentDoor, nextDoor));
            connectedDoors.add(nextDoor);
            currentDoor = nextDoor;
        }

        return solution;
    }

    // 选择下一个要连接的门
    private Door selectNextDoor(Door currentDoor, List<Door> allDoors, Set<Door> connectedDoors,
                                HashMap<DoorPair, Double> pheromones) {
        List<Door> candidates = new ArrayList<>();
        List<Double> probabilities = new ArrayList<>();
        double totalProbability = 0.0;

        for (Door door : allDoors) {
            if (!connectedDoors.contains(door)) {
                candidates.add(door);

                DoorPair pair = new DoorPair(currentDoor, door);
                double pheromone = pheromones.getOrDefault(pair, 0.1);
                double distance = Connector.manhattanDistance(currentDoor.getPoint(), door.getPoint());
                double heuristic = 1.0 / distance; // 启发式信息（距离的倒数）

                // 同一房间的门减少选择概率
                if (currentDoor.getRoom() == door.getRoom()) {
                    heuristic *= 0.5;
                }

                double probability = Math.pow(pheromone, ALPHA) * Math.pow(heuristic, BETA);
                probabilities.add(probability);
                totalProbability += probability;
            }
        }

        // 轮盘赌选择
        double rand = Math.random() * totalProbability;
        double cumulativeProbability = 0.0;

        for (int i = 0; i < candidates.size(); i++) {
            cumulativeProbability += probabilities.get(i);
            if (cumulativeProbability >= rand) {
                return candidates.get(i);
            }
        }

        // 默认选择最后一个候选门
        return candidates.get(candidates.size() - 1);
    }

    // 评估解决方案质量（总路径长度）
    private double evaluateSolution(List<DoorPair> solution) {
        double totalDistance = 0.0;

        for (DoorPair pair : solution) {
            totalDistance += Connector.manhattanDistance(pair.getDoor1().getPoint(), pair.getDoor2().getPoint());

            // 同一房间的门增加惩罚
            if (pair.getDoor1().getRoom() == pair.getDoor2().getRoom()) {
                totalDistance += 100;
            }
        }

        return totalDistance;
    }

    // 更新信息素
    private void updatePheromones(HashMap<DoorPair, Double> pheromones, List<List<DoorPair>> antSolutions) {
        // 信息素蒸发
        for (Map.Entry<DoorPair, Double> entry : pheromones.entrySet()) {
            pheromones.put(entry.getKey(), entry.getValue() * (1 - RHO));
        }

        // 信息素增加
        for (List<DoorPair> solution : antSolutions) {
            double solutionQuality = evaluateSolution(solution);
            double pheromoneDeposit = Q / solutionQuality;

            for (DoorPair pair : solution) {
                double currentPheromone = pheromones.getOrDefault(pair, 0.0);
                pheromones.put(pair, currentPheromone + pheromoneDeposit);
            }
        }
    }
}