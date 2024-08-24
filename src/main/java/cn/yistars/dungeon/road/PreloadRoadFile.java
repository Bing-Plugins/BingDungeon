package cn.yistars.dungeon.road;

import cn.yistars.dungeon.BingDungeon;
import cn.yistars.dungeon.room.door.DoorType;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.transform.AffineTransform;
import lombok.Getter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;

@Getter
public class PreloadRoadFile {
    private final String id;
    private final HashSet<PreloadRoad> preloadRoads = new HashSet<>();
    private final HashSet<DoorType> originalFacings = new HashSet<>();
    private final Clipboard originalClipboard;
    private final Integer yOffset;

    public PreloadRoadFile(String id) {
        this.id = id;
        this.yOffset = BingDungeon.instance.Roads.getConfig().getInt(id + ".y-offset");

        File file = BingDungeon.instance.getDataFolder().toPath().resolve("roads/" + id + ".schem").toFile();
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            originalClipboard = reader.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (String facingStr : BingDungeon.instance.Roads.getConfig().getStringList(id + ".facing")) {
            originalFacings.add(DoorType.valueOf(facingStr.toUpperCase()));
        }

        if (originalFacings.size() >= 4) {
            preloadRoads.add(new PreloadRoad(id, originalClipboard, yOffset, originalFacings));
            return;
        }

        for (int i = 0; i < 4; i++) {
            // 旋转剪贴板
            Clipboard clipboard = originalClipboard.transform(new AffineTransform().rotateY(i * 90));
            clipboard.setOrigin(clipboard.getMinimumPoint());

            HashSet<DoorType> facings = new HashSet<>();
            for (DoorType facing : originalFacings) {
                facings.add(facing.rotate(i * 90));
            }

            preloadRoads.add(new PreloadRoad(id, clipboard, yOffset, facings));
        }
    }
}
