package cn.yistars.dungeon.arena;

import cn.yistars.dungeon.BingDungeon;
import cn.yistars.dungeon.arena.map.ArenaMap;
import cn.yistars.dungeon.road.Road;
import cn.yistars.dungeon.room.Room;
import cn.yistars.dungeon.room.RectangleSeparator;
import cn.yistars.dungeon.room.door.Door;
import cn.yistars.dungeon.setup.RegionType;
import com.infernalsuite.aswm.api.world.SlimeWorld;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.util.*;
import java.util.List;

@Getter
public class Arena {
    private final List<Room> rooms = new ArrayList<>();
    private final List<Road> roads = new ArrayList<>();
    private final Integer initRadius = 5;
    private World world;
    private HashSet<Player> players = new HashSet<>();
    private final ArenaMap arenaMap = new ArenaMap(this);

    public Arena() {
        initWorld();

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getWorld().equals(world)) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(getRoom(player.getLocation())));
                    }
                }
            }
        }.runTaskTimerAsynchronously(BingDungeon.instance, 0, 10);
    }

    private void nextStep() {
        new BukkitRunnable() {
            @Override
            public void run() {
                initRoom();
                spawnRoom();
                initDoor();
                initRoad();
                spawnRoad();

                arenaMap.update();
            }
        }.runTaskAsynchronously(BingDungeon.instance);
    }

    private void initWorld() {
        SlimeWorld mirrorWorld = ArenaManager.slimeWorld.clone(
                BingDungeon.instance.getConfig().getString("mirror-world-id", "DungeonMirror-%timestamp%")
                        .replace("%timestamp%", String.valueOf(System.currentTimeMillis()))
        );
        new BukkitRunnable() {
            @Override
            public void run() {
                SlimeWorld mirror = ArenaManager.asp.loadWorld(mirrorWorld, true);
                world = Bukkit.getWorld(mirror.getName());

                nextStep();
            }
        }.runTask(BingDungeon.instance);
    }

    private void initRoom() {
        for (String key : BingDungeon.instance.Rooms.getConfig().getKeys(false)) {
            rooms.add(new Room(key));
        }
    }

    private void spawnRoom() {
        // 随机在圆内绘制点位
        Random random = new Random();
        for (Room room : rooms) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = Math.sqrt(random.nextDouble()) * initRadius;
            int x = (int) (distance * Math.cos(angle));
            int y = (int) (distance * Math.sin(angle));

            room.setPosition(x, y);
        }
        // 分离算法
        RectangleSeparator separator = new RectangleSeparator(rooms);
        separator.separate();
        // 粘贴
        for (Room room : rooms) {
            room.pasting(new BukkitWorld(this.world));
        }
    }

    public RegionType getType(double x, double y) {
        for (Room room : rooms) {
            if (room.getRectangle().contains(x, y)) {
                return RegionType.ROOM;
            }
        }

        for (Road road : roads) {
            if (road.getRectangle().contains(x, y)) {
                return RegionType.ROAD;
            }
        }

        return null;
    }

    public Room getRoom(double x, double y) {
        for (Room room : rooms) {
            if (room.getRectangle().contains(x, y)) {
                return room;
            }
        }
        return null;
    }

    private void initDoor() {
        for (Room room : rooms) {
            room.initDoors();
        }
    }

    // TODO DEBUG 用获取位置
    public String getRoom(Location location) {
        for (Room room : rooms) {
            if (room.contains(location)) {
                return room.getId() + " (" + room.getAngle() + "° 旋转) :" + (int) location.getX() / 7 + ", " + (int) location.getZ() / 7;
            }
        }

        for (Road road : roads) {
            if (road.contains(location)) {
                return "走廊 " + road.getFacings() + ": " + (int) location.getX() / 7 + ", " + (int) location.getZ() / 7;
            }
        }
        return "空区域";
    }

    private void spawnRoad() {
        System.out.println("生成道路");
        for (Road road : roads) {
            road.initFacing(this);
            road.pasting(BukkitAdapter.adapt(world));
        }
    }

    public HashSet<Rectangle> getRectangles() {
        HashSet<Rectangle> rectangles = new HashSet<>();
        for (Room room : rooms) {
            rectangles.add(room.getRectangle());
        }
        return rectangles;
    }

    private void initRoad() {
        List<Door> allDoors = new ArrayList<>();
        for (Room room : rooms) {
            allDoors.addAll(room.getDoors());
        }

        // Step 1: Calculate all distances between doors
        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < allDoors.size(); i++) {
            for (int j = i + 1; j < allDoors.size(); j++) {
                Door door1 = allDoors.get(i);
                Door door2 = allDoors.get(j);
                if (door1.getRoom() != door2.getRoom()) {
                    double distance = door1.getPoint().distance(door2.getPoint());
                    edges.add(new Edge(door1, door2, distance));
                }
            }
        }

        // Step 2: Use Kruskal's algorithm to find the Minimum Spanning Tree (MST)
        Collections.sort(edges);
        UnionFind unionFind = new UnionFind(allDoors.size());
        for (Edge edge : edges) {
            int root1 = unionFind.find(allDoors.indexOf(edge.door1));
            int root2 = unionFind.find(allDoors.indexOf(edge.door2));
            if (root1 != root2) {
                unionFind.union(root1, root2);
                createRoad(edge.door1, edge.door2);
            }
        }
    }

    private void createRoad(Door door1, Door door2) {
        Point p1 = door1.getPoint();
        Point p2 = door2.getPoint();
        List<Point> path = findPath(getRectangles(), p1, p2);

        for (int i = 0; i < path.size() - 1; i++) {
            Point start = path.get(i);
            Point end = path.get(i + 1);
            Road road = new Road();
            road.setPosition(start.x, start.y);
            roads.add(road);
        }
    }

    private class Edge implements Comparable<Edge> {
        Door door1, door2;
        double distance;

        Edge(Door door1, Door door2, double distance) {
            this.door1 = door1;
            this.door2 = door2;
            this.distance = distance;
        }

        @Override
        public int compareTo(Edge other) {
            return Double.compare(this.distance, other.distance);
        }
    }

    private class UnionFind {
        private int[] parent, rank;

        UnionFind(int size) {
            parent = new int[size];
            rank = new int[size];
            for (int i = 0; i < size; i++) {
                parent[i] = i;
                rank[i] = 0;
            }
        }

        int find(int p) {
            if (parent[p] != p) {
                parent[p] = find(parent[p]);
            }
            return parent[p];
        }

        void union(int p, int q) {
            int rootP = find(p);
            int rootQ = find(q);
            if (rootP != rootQ) {
                if (rank[rootP] > rank[rootQ]) {
                    parent[rootQ] = rootP;
                } else if (rank[rootP] < rank[rootQ]) {
                    parent[rootP] = rootQ;
                } else {
                    parent[rootQ] = rootP;
                    rank[rootP]++;
                }
            }
        }
    }

    // A*路径查找方法
    public ArrayList<Point> findPath(HashSet<Rectangle> rectangles, Point p1, Point p2) {
        ArrayList<Point> resultPath = new ArrayList<>();
        HashSet<Point> visited = new HashSet<>();
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Map<Point, Point> parentMap = new HashMap<>();

        openSet.offer(new Node(p1, 0, manhattanDistance(p1, p2)));
        parentMap.put(p1, null);

        while (!openSet.isEmpty()) {
            Node currentNode = openSet.poll();
            Point current = currentNode.point;

            if (current.equals(p2)) {
                break;
            }

            visited.add(current);

            for (Point neighbor : getNeighbors(current)) {
                if (isValidPoint(neighbor, rectangles, visited)) {
                    int tentativeGCost = currentNode.gCost + 1;
                    Node neighborNode = new Node(neighbor, tentativeGCost, manhattanDistance(neighbor, p2));

                    if (!parentMap.containsKey(neighbor) || tentativeGCost < neighborNode.gCost) {
                        parentMap.put(neighbor, current);
                        openSet.offer(neighborNode);
                    }
                }
            }
        }

        // 追踪路径
        Point step = p2;
        while (step != null && parentMap.containsKey(step)) {
            resultPath.add(step);
            step = parentMap.get(step);
        }
        resultPath.add(p1); // 加入起始点

        return resultPath;
    }

    // 计算曼哈顿距离（用于启发式函数）
    private int manhattanDistance(Point p1, Point p2) {
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    }

    private boolean isValidPoint(Point point, HashSet<Rectangle> rectangles, HashSet<Point> visited) {
        if (visited.contains(point)) return false;
        for (Rectangle rectangle : rectangles) {
            if (rectangle.contains(point)) return false;
        }
        return true;
    }

    private List<Point> getNeighbors(Point point) {
        List<Point> neighbors = new ArrayList<>();
        neighbors.add(new Point(point.x + 1, point.y));
        neighbors.add(new Point(point.x - 1, point.y));
        neighbors.add(new Point(point.x, point.y + 1));
        neighbors.add(new Point(point.x, point.y - 1));
        return neighbors;
    }

    class Node implements Comparable<Node> {
        Point point;
        int gCost, hCost;

        Node(Point point, int gCost, int hCost) {
            this.point = point;
            this.gCost = gCost;
            this.hCost = hCost;
        }

        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.gCost + this.hCost, other.gCost + other.hCost);
        }
    }
}
