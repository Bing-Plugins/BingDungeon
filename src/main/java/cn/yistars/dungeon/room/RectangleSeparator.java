package cn.yistars.dungeon.room;

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
                    force.x += rooms.get(j).getRectangle().x - rooms.get(i).getRectangle().x;
                    force.y += rooms.get(j).getRectangle().y - rooms.get(i).getRectangle().y;
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
        return r1.getRectangle().intersects(r2.getRectangle());
    }

    private void moveRectangle(Room room, Point move) {
        room.setPosition(room.getRectangle().x + move.x, room.getRectangle().y + move.y);
    }
}
