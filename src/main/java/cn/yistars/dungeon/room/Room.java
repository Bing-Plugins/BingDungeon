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
}
