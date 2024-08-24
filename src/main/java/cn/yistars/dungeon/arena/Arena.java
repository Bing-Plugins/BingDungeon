package cn.yistars.dungeon.arena;

import cn.yistars.dungeon.BingDungeon;
import cn.yistars.dungeon.road.Road;
import cn.yistars.dungeon.room.Room;
import cn.yistars.dungeon.room.RectangleSeparator;
import cn.yistars.dungeon.room.door.Door;
import cn.yistars.dungeon.room.door.DoorType;
import cn.yistars.dungeon.setup.RegionType;
import com.infernalsuite.aswm.api.world.SlimeWorld;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
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

public class Arena {
    private final List<Room> rooms = new ArrayList<>();
    private final List<Road> roads = new ArrayList<>();
    private final Integer initRadius = 5;
    private World world;

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
        }.runTaskTimerAsynchronously(BingDungeon.instance, 0, 20);
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

    public RegionType getType(int x, int y) {
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

    public Room getRoom(int x, int y) {
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
                return room.getId() + " (" + room.getAngle() + "° 旋转)";
            }
        }
        return "空区域";
    }

    private void spawnRoad() {
        for (Road road : roads) {
            road.initFacing(this);
            road.pasting(BukkitAdapter.adapt(world));
        }
    }

    /*
        TODO 以下内容为实验性内容，存在较大问题，如：断头路，绕着房间跑，怼到房间墙壁的断头路
     */

    private void initRoad() {
        // 连接所有房间的 Door, 获取连接点的坐标
        generatePaths();
    }

    public void generatePaths() {
        Map<Door, Road> doorToRoadMap = new HashMap<>();
        for (Room room : rooms) {
            for (Door door : room.getDoors()) {
                Road road = createInitialRoadForDoor(door);
                doorToRoadMap.put(door, road);
            }
        }

        Map<Door, Map<Door, Integer>> graph = buildGraph(doorToRoadMap);

        Set<Door> visited = new HashSet<>();
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingInt(e -> e.weight));

        Door start = graph.keySet().iterator().next();
        visited.add(start);
        for (Map.Entry<Door, Integer> entry : graph.get(start).entrySet()) {
            pq.add(new Edge(start, entry.getKey(), entry.getValue()));
        }

        while (!pq.isEmpty()) {
            Edge edge = pq.poll();
            if (!visited.contains(edge.to) && !isSameRoom(edge.from, edge.to)) {
                visited.add(edge.to);
                createRoadBetween(doorToRoadMap.get(edge.from), doorToRoadMap.get(edge.to));

                for (Map.Entry<Door, Integer> entry : graph.get(edge.to).entrySet()) {
                    if (!visited.contains(entry.getKey())) {
                        pq.add(new Edge(edge.to, entry.getKey(), entry.getValue()));
                    }
                }
            }
        }

        for (Door door : graph.keySet()) {
            if (!visited.contains(door)) {
                Road nearestRoad = findNearestRoad(doorToRoadMap.get(door));
                createRoadBetween(doorToRoadMap.get(door), nearestRoad);
            }
        }

        removeIsolatedRoads();
        //simplifyParallelRoads();
    }

    private Road createInitialRoadForDoor(Door door) {
        int[] direction = getDirection(door.getType());
        int x = door.getX() + direction[0];
        int z = door.getZ() + direction[1];
        Road road = new Road();
        road.setPosition(x, z);

        Road nearestRoad = findNearestRoad(road);
        if (nearestRoad != null && areAdjacent(road, nearestRoad)) {
            createRoadBetween(road, nearestRoad);
        }

        roads.add(road);
        return road;
    }

    private int[] getDirection(DoorType type) {
        switch (type) {
            case NORTH:
                return new int[]{0, 1};
            case SOUTH:
                return new int[]{0, -1};
            case EAST:
                return new int[]{1, 0};
            case WEST:
                return new int[]{-1, 0};
            default:
                throw new IllegalArgumentException("Unknown DoorType: " + type);
        }
    }

    private Map<Door, Map<Door, Integer>> buildGraph(Map<Door, Road> doorToRoadMap) {
        Map<Door, Map<Door, Integer>> graph = new HashMap<>();

        for (Room room : rooms) {
            for (Door door1 : room.getDoors()) {
                for (Room otherRoom : rooms) {
                    if (room == otherRoom) continue;
                    for (Door door2 : otherRoom.getDoors()) {
                        int distance = calculateDistance(door1, door2);
                        graph.computeIfAbsent(door1, k -> new HashMap<>()).put(door2, distance);
                        graph.computeIfAbsent(door2, k -> new HashMap<>()).put(door1, distance);
                    }
                }
            }
        }

        return graph;
    }

    private int calculateDistance(Door d1, Door d2) {
        return Math.abs(d1.getX() - d2.getX()) + Math.abs(d1.getZ() - d2.getZ());
    }

    private void createRoadBetween(Road road1, Road road2) {
        int x1 = road1.getRectangle().x;
        int z1 = road1.getRectangle().y;
        int x2 = road2.getRectangle().x;
        int z2 = road2.getRectangle().y;

        generateAndAddRoad(x1, z1, x2, z2);
    }

    private void generateAndAddRoad(int x1, int z1, int x2, int z2) {
        if (x1 != x2) {
            for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
                if (isValidRoad(x, z1)) {
                    Road road = new Road();
                    road.setPosition(x, z1);
                    roads.add(road);
                }
            }
        }

        if (z1 != z2) {
            for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
                if (isValidRoad(x2, z)) {
                    Road road = new Road();
                    road.setPosition(x2, z);
                    roads.add(road);
                }
            }
        }
    }

    private boolean isValidRoad(int x, int z) {
        for (Room room : rooms) {
            Rectangle rect = room.getRectangle();
            if (rect.contains(x, z)) {
                return false;
            }
        }
        return true;
    }

    private boolean isSameRoom(Door d1, Door d2) {
        return d1.getRoom().equals(d2.getRoom());
    }

    private Road findNearestRoad(Road road) {
        Road nearestRoad = null;
        int minDistance = Integer.MAX_VALUE;

        for (Road otherRoad : roads) {
            if (road == otherRoad) continue;
            int distance = Math.abs(road.getRectangle().x - otherRoad.getRectangle().x) +
                    Math.abs(road.getRectangle().y - otherRoad.getRectangle().y);
            if (distance < minDistance) {
                minDistance = distance;
                nearestRoad = otherRoad;
            }
        }

        return nearestRoad;
    }

    private void removeIsolatedRoads() {
        Set<Road> connectedRoads = new HashSet<>();
        for (Road road : roads) {
            if (isConnected(road)) {
                connectedRoads.add(road);
            }
        }
        roads.retainAll(connectedRoads);
    }

    private boolean isConnected(Road road) {
        for (Road otherRoad : roads) {
            if (road != otherRoad && areAdjacent(road, otherRoad)) {
                return true;
            }
        }
        return false;
    }

    private boolean areAdjacent(Road r1, Road r2) {
        return Math.abs(r1.getRectangle().x - r2.getRectangle().x) +
                Math.abs(r1.getRectangle().y - r2.getRectangle().y) == 1;
    }

    private void simplifyParallelRoads() {
        Set<Road> roadsToRemove = new HashSet<>();
        for (Road road1 : roads) {
            for (Road road2 : roads) {
                if (road1 != road2 && isParallel(road1, road2)) {
                    mergeRoads(road1, road2);
                    roadsToRemove.add(road2);
                }
            }
        }
        roads.removeAll(roadsToRemove);
    }

    private boolean isParallel(Road r1, Road r2) {
        return (r1.getRectangle().x == r2.getRectangle().x && Math.abs(r1.getRectangle().y - r2.getRectangle().y) == 1) ||
                (r1.getRectangle().y == r2.getRectangle().y && Math.abs(r1.getRectangle().x - r2.getRectangle().x) == 1);
    }

    private void mergeRoads(Road r1, Road r2) {
        r1.getRectangle().setSize(
                Math.max(r1.getRectangle().width, r2.getRectangle().width),
                Math.max(r1.getRectangle().height, r2.getRectangle().height)
        );
    }

    private static class Edge {
        Door from;
        Door to;
        int weight;

        Edge(Door from, Door to, int weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
    }
}
