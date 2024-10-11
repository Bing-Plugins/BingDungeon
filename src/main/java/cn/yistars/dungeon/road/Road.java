package cn.yistars.dungeon.road;

import cn.yistars.dungeon.BingDungeon;
import cn.yistars.dungeon.arena.Arena;
import cn.yistars.dungeon.room.Room;
import cn.yistars.dungeon.room.door.Door;
import cn.yistars.dungeon.room.door.DoorType;
import cn.yistars.dungeon.setup.RegionType;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import lombok.Getter;
import org.bukkit.Location;

import java.awt.*;
import java.util.HashSet;

@Getter
public class Road {
    private final Arena arena;
    private final Rectangle rectangle;
    private final HashSet<DoorType> facings = new HashSet<>();
    private PreloadRoad preloadRoad;
    private Boolean isFind = false;

    public Road(Arena arena) {
        this.arena = arena;
        this.rectangle = new Rectangle(0, 0, 1, 1);
    }

    public void initFacing(Arena arena) {
        // 获取东南西北有无 Door 或 Road
        checkFacing(arena, rectangle.x, rectangle.y + 1, DoorType.NORTH);
        checkFacing(arena, rectangle.x, rectangle.y - 1, DoorType.SOUTH);

        checkFacing(arena, rectangle.x + 1, rectangle.y, DoorType.EAST);
        checkFacing(arena, rectangle.x - 1, rectangle.y, DoorType.WEST);


        preloadRoad = RoadManager.getPreloadRoads(facings).stream().findFirst().orElse(null);
    }

    private void checkFacing(Arena arena, double x, double y, DoorType facing) {
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

    public void setPosition(int x, int y) {
        this.rectangle.setLocation(x, y);
    }

    public void pasting(World world) {
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
            Operation operation = new ClipboardHolder(preloadRoad.getClipboard())
                    .createPaste(editSession)
                    .to(BlockVector3.at(rectangle.x * BingDungeon.instance.getConfig().getInt("unit-size"), 100 - preloadRoad.getYOffset(), rectangle.y * BingDungeon.instance.getConfig().getInt("unit-size")))
                    .build();
            Operations.complete(operation);
        }
    }

    // TODO 测试用
    public boolean contains(Location location) {
        Rectangle realRectangle = new Rectangle(rectangle.x * BingDungeon.instance.getConfig().getInt("unit-size"), rectangle.y * BingDungeon.instance.getConfig().getInt("unit-size"), rectangle.width * BingDungeon.instance.getConfig().getInt("unit-size"), rectangle.height * BingDungeon.instance.getConfig().getInt("unit-size"));
        return realRectangle.contains(location.getBlockX(), location.getBlockZ());
    }

    public void find() {
        if (isFind) return;
        isFind = true;
        arena.getArenaMap().update();
    }
}