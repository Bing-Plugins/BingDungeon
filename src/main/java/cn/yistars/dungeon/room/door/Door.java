package cn.yistars.dungeon.room.door;

import lombok.Getter;
import org.bukkit.Location;

@Getter
public class Door {
    private final DoorType type;
    private final int x, z;
    private final Location location;

    public Door(DoorType type, int x, int z, Location location) {
        this.type = type;
        this.x = x;
        this.z = z;
        this.location = location;
    }

}
