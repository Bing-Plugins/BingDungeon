package cn.yistars.dungeon.arena;

import cn.yistars.dungeon.BingDungeon;
import cn.yistars.dungeon.room.Room;
import cn.yistars.dungeon.room.RectangleSeparator;
import com.infernalsuite.aswm.api.world.SlimeWorld;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Arena {
    private final List<Room> rooms = new ArrayList<>();
    private final Integer initRadius = 5;
    private World world;

    public Arena() {
        initWorld();
        initRoom();
        spawnRoom();
    }

    private void initWorld() {
        SlimeWorld mirrorWorld = ArenaManager.slimeWorld.clone(
                BingDungeon.instance.getConfig().getString("mirror-world-id", "DungeonMirror-%timestamp%")
                        .replace("%timestamp%", String.valueOf(System.currentTimeMillis()))
        );
        SlimeWorld mirror = ArenaManager.asp.loadWorld(mirrorWorld, true);

        this.world = Bukkit.getWorld(mirror.getName());
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
}
