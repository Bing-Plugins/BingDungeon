package cn.yistars.dungeon.arena;

import cn.yistars.dungeon.BingDungeon;
import cn.yistars.dungeon.arena.map.ArenaMap;
import cn.yistars.dungeon.init.DoorConnector;
import cn.yistars.dungeon.road.Road;
import cn.yistars.dungeon.room.Room;
import cn.yistars.dungeon.init.RectangleSeparator;
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
    private final HashSet<Player> players = new HashSet<>();
    private final ArenaMap arenaMap = new ArenaMap(this);

    public Arena() {
        new BukkitRunnable() {
            @Override
            public void run() {
                initWorld();
            }
        }.runTaskAsynchronously(BingDungeon.instance);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getWorld().equals(world)) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(getDebugLoc(player.getLocation())));
                    }
                }
            }
        }.runTaskTimerAsynchronously(BingDungeon.instance, 0, 10);
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void findArea(Location location) {
        // 使临近区域显示
        RegionType regionType = getType(location);
        if (regionType == null) return;

        int x, z;
        switch (regionType) {
            case ROAD:
                Road road = getRoad(location);
                x = road.getRectangle().x;
                z = road.getRectangle().y;
                break;
            case ROOM:
                Room room = getRoom(location);
                x = room.getRectangle().x;
                z = room.getRectangle().y;
                break;
            default:
                return;
        }

        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = z - 1; j <= z + 1; j++) {
                makeFind(i, j);
            }
        }
    }

    private void makeFind(int x, int z) {
        RegionType regionType = getType(x, z);
        if (regionType == null) return;

        switch (regionType) {
            case ROAD:
                getRoad(x, z).find();
                break;
            case ROOM:
                getRoom(x, z).find();
                break;
        }
    }

    private void nextStep() {
        new BukkitRunnable() {
            @Override
            public void run() {
                initRoom();
                spawnRoom();
                initDoor();
                connectDoor();
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
            rooms.add(new Room(this, key));
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

    private void connectDoor() {
        // A* 路径查找 + 蚁群算法
        DoorConnector connector = new DoorConnector((ArrayList<Room>) rooms);

        // 连接所有门
        HashSet<Point> pathPoints = connector.getResult();
        System.out.println("路径点总数: " + pathPoints.size());

        for (Point point : pathPoints) {
            Road road = new Road(this);
            road.setPosition(point.x, point.y);
            roads.add(road);
        }
    }

    public RegionType getType(Location location) {
        for (Room room : rooms) {
            if (room.contains(location)) {
                return RegionType.ROOM;
            }
        }

        for (Road road : roads) {
            if (road.contains(location)) {
                return RegionType.ROAD;
            }
        }

        return null;
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

    public Room getRoom(Location location) {
        for (Room room : rooms) {
            if (room.contains(location)) {
                return room;
            }
        }
        return null;
    }

    public Road getRoad(double x, double y) {
        for (Road road : roads) {
            if (road.getRectangle().contains(x, y)) {
                return road;
            }
        }
        return null;
    }

    public Road getRoad(Location location) {
        for (Road road : roads) {
            if (road.contains(location)) {
                return road;
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
    public String getDebugLoc(Location location) {
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
        for (Road road : roads) {
            road.initFacing(this);
            road.pasting(BukkitAdapter.adapt(world));
        }
    }
}
