package cn.yistars.dungeon.room;

import lombok.Getter;

import java.awt.*;

@Getter
public class Room {
    private final RoomType type;
    private final Rectangle rectangle;

    public Room(RoomType type, Integer width, Integer height) {
        this.type = type;
        this.rectangle = new Rectangle(0, 0, width, height);
    }

    public void setPosition(int x, int y) {
        this.rectangle.setLocation(x, y);
    }

    public boolean overlaps(Room other) {
        return !(this.rectangle.x + this.rectangle.width <= other.rectangle.x ||
                other.rectangle.x + other.rectangle.width <= this.rectangle.x ||
                this.rectangle.y + this.rectangle.height <= other.rectangle.y ||
                other.rectangle.y + other.rectangle.height <= this.rectangle.y);
    }
}
