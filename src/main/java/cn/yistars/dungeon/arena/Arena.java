package cn.yistars.dungeon.arena;

import cn.yistars.dungeon.room.Room;
import cn.yistars.dungeon.room.RoomType;
import cn.yistars.dungeon.room.RectangleSeparator;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Arena {
    private final List<Room> rooms = new ArrayList<>();
    private final Integer initRadius = 3;

    public Arena() {
        rooms.add(new Room(RoomType.START, 1, 1));
        rooms.add(new Room(RoomType.NORMAL, 3, 3));
        rooms.add(new Room(RoomType.NORMAL, 4, 2));
        rooms.add(new Room(RoomType.NORMAL, 2, 3));
        rooms.add(new Room(RoomType.NEXT, 1, 1));

        initArena();
    }

    public void initArena() {
        // 在半径为 initRadius 的圆形内，必须是圆形，随机生成若干个坐标点
        /*
        Random random = new Random();
        for (Room room : rooms) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = Math.sqrt(random.nextDouble()) * initRadius;
            int x = (int) (distance * Math.cos(angle));
            int y = (int) (distance * Math.sin(angle));

            room.setPosition(x, y);

            System.out.println("初始化房间坐标点: " + x + ", " + y);
            System.out.println("初始化房间宽度: " + room.getWidth() + " 高度: " + room.getHeight());
            System.out.println("===========");
        }

         */

        List<Rectangle> rectangles = new ArrayList<>();
        rectangles.add(new Rectangle(0, 0, 50, 50));
        rectangles.add(new Rectangle(25, 25, 50, 50));
        rectangles.add(new Rectangle(50, 50, 50, 50));

        RectangleSeparator separator = new RectangleSeparator(rectangles);
        separator.separate();

        for (Rectangle rect : rectangles) {
            System.out.println("Rectangle at (" + rect.x + ", " + rect.y + ")");
        }
    }
}
