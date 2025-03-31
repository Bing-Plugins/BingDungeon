package cn.yistars.dungeon.init;

import cn.yistars.dungeon.room.Room;

import java.awt.*;
import java.util.List;

public class RectangleSeparator {
    private final List<Room> rooms;

    public RectangleSeparator(List<Room> rooms) {
        this.rooms = rooms;
    }

    public void separate() {
        boolean anyOverlap;
        do {
            anyOverlap = false;
            for (int i = 0; i < rooms.size(); i++) {
                Point force = new Point(0, 0);
                int overlapCounter = 0;
                for (int j = 0; j < rooms.size(); j++) {
                    if (i == j) continue;
                    if (!isOverlapped(rooms.get(i), rooms.get(j))) continue;
                    force.x += rooms.get(j).getMarginRectangle().x - rooms.get(i).getMarginRectangle().x;
                    force.y += rooms.get(j).getMarginRectangle().y - rooms.get(i).getMarginRectangle().y;
                    overlapCounter++;
                }
                if (overlapCounter == 0) continue;
                force.x /= overlapCounter;
                force.y /= overlapCounter;
                force.x *= -1;
                force.y *= -1;
                moveRectangle(rooms.get(i), force);
                anyOverlap = true;
            }
        } while (anyOverlap);
    }

    private boolean isOverlapped(Room r1, Room r2) {
        return r1.getMarginRectangle().intersects(r2.getMarginRectangle());
    }

    private void moveRectangle(Room room, Point move) {
        room.setPosition(room.getMarginRectangle().x + move.x, room.getMarginRectangle().y + move.y);
    }
}
