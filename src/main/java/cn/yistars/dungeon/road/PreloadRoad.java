package cn.yistars.dungeon.road;

import cn.yistars.dungeon.room.door.DoorType;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import lombok.Getter;

import java.util.HashSet;

@Getter
public class PreloadRoad {
    private final String id;
    private final Clipboard clipboard;
    private final Integer yOffset;
    private final HashSet<DoorType> facings;

    public PreloadRoad(String id, Clipboard clipboard, Integer yOffset, HashSet<DoorType> facings) {
        this.id = id;
        this.clipboard = clipboard;
        this.yOffset = yOffset;
        this.facings = facings;
    }
}
