package cn.yistars.dungeon.room;

import cn.yistars.dungeon.BingDungeon;
import cn.yistars.dungeon.room.door.Door;
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
public class Room {
    private final RoomType type;
    private final Rectangle rectangle;
    private final Clipboard clipboard;
    private final Rectangle marginRectangle;
    private final HashSet<Door> doors = new HashSet<>();

    public Room(String id) {
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

        File file = BingDungeon.instance.getDataFolder().toPath().resolve("rooms/" + id + ".schem").toFile();
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
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
                    .to(BlockVector3.at(rectangle.x * BingDungeon.instance.getConfig().getInt("unit-size"), 100, rectangle.y * BingDungeon.instance.getConfig().getInt("unit-size")))
                    .build();
            Operations.complete(operation);
        }
    }
}
