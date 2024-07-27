package cn.yistars.dungeon.room;

import java.awt.*;
import java.util.List;

public class RectangleSeparator {
    private final List<Rectangle> rectangles;

    public RectangleSeparator(List<Rectangle> rectangles) {
        this.rectangles = rectangles;
    }

    public void separate() {
        boolean anyOverlap;
        do {
            anyOverlap = false;
            for (int i = 0; i < rectangles.size(); i++) {
                Point force = new Point(0, 0);
                int overlapCounter = 0;
                for (int j = 0; j < rectangles.size(); j++) {
                    if (i == j) continue;
                    if (!isOverlapped(rectangles.get(i), rectangles.get(j))) continue;
                    force.x += rectangles.get(j).x - rectangles.get(i).x;
                    force.y += rectangles.get(j).y - rectangles.get(i).y;
                    overlapCounter++;
                }
                if (overlapCounter == 0) continue;
                force.x /= overlapCounter;
                force.y /= overlapCounter;
                force.x *= -1;
                force.y *= -1;
                moveRectangle(rectangles.get(i), force);
                anyOverlap = true;
            }
        } while (anyOverlap);
    }

    private boolean isOverlapped(Rectangle r1, Rectangle r2) {
        return r1.intersects(r2);
    }

    private void moveRectangle(Rectangle rectangle, Point move) {
        rectangle.x += move.x;
        rectangle.y += move.y;
    }
}
