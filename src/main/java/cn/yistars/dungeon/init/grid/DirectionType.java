package cn.yistars.dungeon.init.grid;

import java.awt.*;
import java.util.HashSet;
import java.util.Random;

public enum DirectionType {
    UP, // 左上=
    DOWN, // 下
    LEFT, // 左
    RIGHT; // 右

    public HashSet<Point> getRandomRegion(Random random, int grid_radius) {
        HashSet<Point> region = new HashSet<>();

        // 随机矩形大小，最大为 5，最小 1
        int rectSizeX = 1 + 2 * random.nextInt(2);
        int rectSizeY = 1 + 2 * random.nextInt(2);
        if (rectSizeX == 1) {
            rectSizeY = 3 + 2 * random.nextInt(1);
        }

        switch (this) {
            case UP:
                int x = -grid_radius + random.nextInt(grid_radius * 2 - rectSizeX);
                int y = grid_radius;

                if (x != -grid_radius && x % 2 == 0 ) x -= 1; // 确保 x 是奇数

                for (int dx = 0; dx < rectSizeX; dx++) {
                    for (int dy = 0; dy < rectSizeY; dy++) {
                        region.add(new Point(x + dx, y - dy));
                    }
                }

                break;
            case DOWN:
                x = -grid_radius + random.nextInt(grid_radius * 2 - rectSizeX);
                y = -grid_radius;

                if (x != -grid_radius && x % 2 == 0 ) x -= 1; // 确保 x 是奇数

                for (int dx = 0; dx < rectSizeX; dx++) {
                    for (int dy = 0; dy < rectSizeY; dy++) {
                        region.add(new Point(x + dx, y + dy));
                    }
                }

                break;
            case LEFT:
                x = -grid_radius;
                y = -grid_radius + random.nextInt(grid_radius * 2);

                if (y != -grid_radius && y % 2 == 0 ) y -= 1; // 确保 y 是奇数

                for (int dx = 0; dx < rectSizeX; dx++) {
                    for (int dy = 0; dy < rectSizeY; dy++) {
                        region.add(new Point(x + dx, y - dy));
                    }
                }
                break;
            case RIGHT:
                x = grid_radius;
                y = -grid_radius + random.nextInt(grid_radius * 2);

                if (y != -grid_radius && y % 2 == 0 ) y -= 1; // 确保 y 是奇数

                for (int dx = 0; dx < rectSizeX; dx++) {
                    for (int dy = 0; dy < rectSizeY; dy++) {
                        region.add(new Point(x - dx, y - dy));
                    }
                }

                break;
        }

        return region;
    }
}
