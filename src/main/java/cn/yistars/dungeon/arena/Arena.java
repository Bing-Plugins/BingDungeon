package cn.yistars.dungeon.arena;

import cn.yistars.dungeon.room.Room;
import cn.yistars.dungeon.room.RectangleSeparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Arena {
    private final List<Room> rooms = new ArrayList<>();
    private final Integer initRadius = 5;

    public Arena() {
        // TODO 读取 room 数据

        initArena();
    }

    public void initArena() {
        Random random = new Random();
        for (Room room : rooms) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = Math.sqrt(random.nextDouble()) * initRadius;
            int x = (int) (distance * Math.cos(angle));
            int y = (int) (distance * Math.sin(angle));

            room.setPosition(x, y);
        }

        RectangleSeparator separator = new RectangleSeparator(rooms);
        separator.separate();
    }
}
