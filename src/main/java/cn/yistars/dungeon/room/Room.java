package cn.yistars.dungeon.room;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import lombok.Getter;

import java.awt.*;

@Getter
public class Room {
    private final RoomType type;
    private final Rectangle rectangle;
    private final Clipboard clipboard;

    public Room(RoomType type, Integer width, Integer height, Clipboard clipboard) {
        this.type = type;
        this.rectangle = new Rectangle(0, 0, width, height);
        this.clipboard = clipboard;
    }

    public void setPosition(int x, int y) {
        this.rectangle.setLocation(x, y);
    }

    public void pasting(World world) {
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(rectangle.x, 100, rectangle.y))
                    .build();
            Operations.complete(operation);
        }
    }
}
