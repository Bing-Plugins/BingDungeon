package cn.yistars.dungeon.road;

import cn.yistars.dungeon.BingDungeon;
import cn.yistars.dungeon.arena.Arena;
import cn.yistars.dungeon.room.Room;
import cn.yistars.dungeon.room.door.Door;
import cn.yistars.dungeon.room.door.DoorType;
import cn.yistars.dungeon.setup.RegionType;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
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

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;

@Getter
public class Road {
    private final Rectangle rectangle;
    private Clipboard clipboard;
    private Integer yOffset;
    private HashSet<DoorType> facings = new HashSet<>();

    public Road() {
        this.rectangle = new Rectangle(0, 0, 1, 1);
    }

    public void initFacing(Arena arena) {
        // 获取东南西北有无 Door 或 Road
        checkFacing(arena, rectangle.x + 1, rectangle.y, DoorType.NORTH);
        checkFacing(arena, rectangle.x, rectangle.y + 1, DoorType.EAST);
        checkFacing(arena, rectangle.x - 1, rectangle.y, DoorType.SOUTH);
        checkFacing(arena, rectangle.x, rectangle.y - 1, DoorType.WEST);

        // 决定 ID
        for (String key : BingDungeon.instance.Roads.getConfig().getConfigurationSection("").getKeys(false)) {
            if (facings.size() != BingDungeon.instance.Roads.getConfig().getStringList(key + ".facings").size()) continue;
            boolean match = true;
            for (DoorType facing : facings) {
                if (!BingDungeon.instance.Roads.getConfig().getStringList(key + ".facings").contains(facing.toString())) {
                    match = false;
                    break;
                }
            }
            if (match) {
                setID(key);
                return;
            }
        }
        // TODO 否则默认
        //System.out.println("默认走廊: " + facings);
        setID(BingDungeon.instance.getConfig().getString("default-road"));
    }

    private void checkFacing(Arena arena, int x, int y, DoorType facing) {
        RegionType regionType = arena.getType(x, y);
        if (regionType == null) return;

        switch (regionType) {
            case ROAD:
                facings.add(facing);
                break;
            case ROOM:
                Room room = arena.getRoom(x, y);
                for (Door door : room.getDoors()) {
                    if (!door.getType().equals(facing.getOpposite())) continue;
                    if (door.getX() == x && door.getZ() == y) facings.add(facing);
                }
                break;
        }
    }

    private void setID(String id) {
        this.yOffset = BingDungeon.instance.Roads.getConfig().getInt(id + ".y-offset");

        File file = BingDungeon.instance.getDataFolder().toPath().resolve("roads/" + id + ".schem").toFile();
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPosition(int x, int y) {
        this.rectangle.setLocation(x, y);
    }

    public void pasting(World world) {
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(rectangle.x * BingDungeon.instance.getConfig().getInt("unit-size"), 100 - yOffset, rectangle.y * BingDungeon.instance.getConfig().getInt("unit-size")))
                    .build();
            Operations.complete(operation);
        }
    }
}