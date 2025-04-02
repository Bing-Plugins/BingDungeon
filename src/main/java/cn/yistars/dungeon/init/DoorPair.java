package cn.yistars.dungeon.init;

import cn.yistars.dungeon.room.door.Door;
import lombok.Getter;

import java.util.Objects;

@Getter
public class DoorPair {
    private final Door door1;
    private final Door door2;

    public DoorPair(Door door1, Door door2) {
        // 确保门的顺序一致，便于HashMap比较
        if (door1.getPoint().x < door2.getPoint().x ||
                (door1.getPoint().x == door2.getPoint().x && door1.getPoint().y < door2.getPoint().y)) {
            this.door1 = door1;
            this.door2 = door2;
        } else {
            this.door1 = door2;
            this.door2 = door1;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DoorPair doorPair = (DoorPair) o;
        return (Objects.equals(door1, doorPair.door1) && Objects.equals(door2, doorPair.door2)) ||
                (Objects.equals(door1, doorPair.door2) && Objects.equals(door2, doorPair.door1));
    }

    @Override
    public int hashCode() {
        // 确保无论门的顺序如何，哈希值都相同
        return Objects.hash(door1, door2) + Objects.hash(door2, door1);
    }
}
