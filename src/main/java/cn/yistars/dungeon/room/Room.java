package cn.yistars.dungeon.room;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Room {
    private final RoomType type;
    private final Integer width, height;
    @Setter
    private Integer x, y;

    public Room(RoomType type, Integer weight, Integer height) {
        this.type = type;
        this.width = weight;
        this.height = height;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean overlaps(Room other) {
        return !(this.x + this.width <= other.x ||
                other.x + other.width <= this.x ||
                this.y + this.height <= other.y ||
                other.y + other.height <= this.y);
    }
}
