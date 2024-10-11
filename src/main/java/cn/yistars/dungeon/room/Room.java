package cn.yistars.dungeon.room;

import cn.yistars.dungeon.BingDungeon;
import cn.yistars.dungeon.arena.Arena;
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
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

@Getter
public class Room {
    private final Arena arena;
    private final String id;
    private final RoomType type;
    private Rectangle rectangle;
    private Clipboard clipboard;
    private Rectangle marginRectangle;
    private final HashSet<Door> doors = new HashSet<>();
    private final Integer yOffset;
    private final Integer angle;
    private Boolean isFind = false;

    public Room(Arena arena, String id) {
        this.arena = arena;
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

        // 随机从 0，90，180，270 选择一个设置为 angle
        int[] angles = {0, 90, 180, 270};
        this.angle = angles[new Random().nextInt(angles.length)];

        rotate();
    }

    // 旋转
    private void rotate() {
        int width = rectangle.width;
        int height = rectangle.height;

        switch (this.angle) {
            case 90: case 270:
                // 旋转 90 度
                this.rectangle = new Rectangle(rectangle.x, rectangle.y, height, width);
                this.marginRectangle = new Rectangle(
                        rectangle.x,
                        rectangle.y,
                        height + BingDungeon.instance.getConfig().getInt("unit-margin") * 2,
                        width + BingDungeon.instance.getConfig().getInt("unit-margin") * 2
                );
                break;
        }

        // 旋转 clipboard
        clipboard = clipboard.transform(new AffineTransform().rotateY(this.angle));
        clipboard.setOrigin(clipboard.getMinimumPoint());
    }

    public void initDoors() {
        for (String doorText : BingDungeon.instance.Rooms.getConfig().getStringList(id + ".doors")) {
            String[] doorInfo = doorText.split(",");
            if (doorInfo.length != 3) continue;
            int x = Integer.parseInt(doorInfo[0]);
            int z = Integer.parseInt(doorInfo[1]);
            DoorType type = DoorType.valueOf(doorInfo[2].toUpperCase());

            doors.add(getDoor(this.angle, x, z, type));
        }
    }

    private Door getDoor(int angle, int x, int z, DoorType doorType) {
        int doorX, doorZ;

        switch (angle) {
            case 90:
                doorX = z;
                doorZ = -x + rectangle.height - 1;
                break;
            case 180:
                doorX = -x + rectangle.width - 1;
                doorZ = -z + rectangle.height - 1;
                break;
            case 270:
                doorX = -z + rectangle.width - 1;
                doorZ = x;
                break;
            default:
                doorX = x;
                doorZ = z;
        }
        return new Door(this, doorType.rotate(angle), rectangle.x + doorX, rectangle.y + doorZ);
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

    public void find() {
        if (isFind) return;
        isFind = true;
        arena.getArenaMap().update();
    }

    public boolean contains(Location location) {
        Rectangle realRectangle = new Rectangle(rectangle.x * BingDungeon.instance.getConfig().getInt("unit-size"), rectangle.y * BingDungeon.instance.getConfig().getInt("unit-size"), rectangle.width * BingDungeon.instance.getConfig().getInt("unit-size"), rectangle.height * BingDungeon.instance.getConfig().getInt("unit-size"));
        return realRectangle.contains(location.getBlockX(), location.getBlockZ());
    }
}
