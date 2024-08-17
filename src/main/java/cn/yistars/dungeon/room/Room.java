package cn.yistars.dungeon.room;

import cn.yistars.dungeon.BingDungeon;
import cn.yistars.dungeon.room.door.Door;
import cn.yistars.dungeon.room.door.DoorType;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;

@Getter
public class Room {
    private final String id;
    private final RoomType type;
    private final Rectangle rectangle;
    private final Clipboard clipboard;
    private final Rectangle marginRectangle;
    private final HashSet<Door> doors = new HashSet<>();
    private final Integer yOffset;

    public Room(String id) {
        this.id = id;
        this.type = RoomType.valueOf(BingDungeon.instance.Rooms.getConfig().getString(id + ".type", "NORMAL").toUpperCase());
        this.rectangle = new Rectangle(0, 0,
                BingDungeon.instance.Rooms.getConfig().getInt(id + ".width"),
                BingDungeon.instance.Rooms.getConfig().getInt(id + ".length")
        );
        this.marginRectangle = new Rectangle(
                rectangle.x,
                rectangle.y,
                rectangle.width + BingDungeon.instance.getConfig().getInt("unit-margin") * 2,
                rectangle.height + BingDungeon.instance.getConfig().getInt("unit-margin") * 2
        );
        this.yOffset = BingDungeon.instance.Rooms.getConfig().getInt(id + ".y-offset");

        File file = BingDungeon.instance.getDataFolder().toPath().resolve("rooms/" + id + ".schem").toFile();
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void initDoors() {
        for (String doorText : BingDungeon.instance.Rooms.getConfig().getStringList(id + ".doors")) {
            String[] doorInfo = doorText.split(",");
            if (doorInfo.length != 3) continue;
            int x = Integer.parseInt(doorInfo[0]) + rectangle.x;
            int z = Integer.parseInt(doorInfo[1]) + rectangle.y;
            DoorType type = DoorType.valueOf(doorInfo[2].toUpperCase());

            doors.add(new Door(this, type, x, z));
        }
    }

    public void setPosition(int x, int y) {
        this.marginRectangle.setLocation(x, y);
        this.rectangle.setLocation(x + BingDungeon.instance.getConfig().getInt("unit-margin"), y + BingDungeon.instance.getConfig().getInt("unit-margin"));
    }

    public void pasting(World world) {
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(rectangle.x * BingDungeon.instance.getConfig().getInt("unit-size"), 100 - yOffset, rectangle.y * BingDungeon.instance.getConfig().getInt("unit-size")))
                    .build();
            Operations.complete(operation);
        }

        org.bukkit.World bukkitWorld = BukkitAdapter.adapt(world);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = rectangle.x * 7; i < rectangle.x * 7 + rectangle.width * 7; i++) {
                    for (int j = rectangle.y * 7; j < rectangle.y * 7 + rectangle.height * 7; j++) {
                        bukkitWorld.spawnParticle(Particle.SMOKE, i, 100 - yOffset, j, 1, 0, 0, 0, 0);
                    }
                }
            }
        }.runTaskTimerAsynchronously(BingDungeon.instance, 0, 5);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Door door : doors) {
                    bukkitWorld.spawnParticle(Particle.SPIT, door.getX() * 7 + 3, 100 - yOffset, door.getZ() * 7 + 3, 1, 0, 0, 0, 0);
                }
            }
        }.runTaskTimerAsynchronously(BingDungeon.instance, 0, 5);
    }

    // TODO 测试用
    public boolean contains(Location location) {
        return rectangle.contains(location.getBlockX() / BingDungeon.instance.getConfig().getInt("unit-size"), location.getBlockZ() / BingDungeon.instance.getConfig().getInt("unit-size"));
    }
}
