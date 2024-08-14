package cn.yistars.dungeon.room.door;

import lombok.Getter;

@Getter
public class Door {
    private final DoorType type;
    private final int x, y;

    public Door(DoorType type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
    }

}
