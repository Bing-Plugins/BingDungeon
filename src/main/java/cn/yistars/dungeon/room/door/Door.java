package cn.yistars.dungeon.room.door;

import cn.yistars.dungeon.room.Room;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
public class Door {
    private final DoorType type;
    @Setter
    private int x, z;
    private final Room room;

    public Door(Room room, DoorType type, int x, int z) {
        this.room = room;
        this.type = type;
        this.x = x;
        this.z = z;
    }

    public Point getPoint() {
        return switch (type) {
            case NORTH -> new Point(x, z + 1);
            case SOUTH -> new Point(x, z - 1);
            case EAST -> new Point(x + 1, z);
            case WEST -> new Point(x - 1, z);
        };
    }
}
