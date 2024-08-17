package cn.yistars.dungeon.room.door;

import lombok.Getter;
import org.bukkit.Location;

@Getter
public class SetupDoor extends Door {
    private final Location location;

    public SetupDoor(DoorType type, int x, int z, Location location) {
        super(null, type, x, z);

        this.location = location;
    }
}
