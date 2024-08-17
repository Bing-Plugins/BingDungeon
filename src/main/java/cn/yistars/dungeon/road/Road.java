package cn.yistars.dungeon.road;

import cn.yistars.dungeon.BingDungeon;
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

@Getter
public class Road {
    private final Rectangle rectangle;
    private final Clipboard clipboard;
    private final Integer yOffset;

    public Road(String id) {
        this.rectangle = new Rectangle(0, 0, 1, 1);
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