package cn.yistars.dungeon.init.grid;

import cn.yistars.dungeon.BingDungeon;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public enum DirectionType {
    UP, // 上
    DOWN, // 下
    LEFT, // 左
    RIGHT; // 右

    public HashSet<Point> getRandomRegion(Random random, int grid_radius) {
        HashSet<Point> region = new HashSet<>();

        // 可选边长
        List<Integer> rectSizes = BingDungeon.instance.getConfig().getIntegerList("path-only.random-remove-edge-rectangle-sizes");
        // 除了绝对值为 1 的边长
        ArrayList<Integer> validSizes = new ArrayList<>();
        for (int size : rectSizes) if (Math.abs(size) != 1) validSizes.add(size);

        // 随机矩形大小
        int rectSizeX = rectSizes.get(random.nextInt(rectSizes.size()));
        int rectSizeY;
        // 如果 x 绝对值为 1，则随机选择一个有效的矩形大小
        if (Math.abs(rectSizeX) == 1) {
            rectSizeY = validSizes.get(random.nextInt(validSizes.size()));
        } else {
            rectSizeY = rectSizes.get(random.nextInt(rectSizes.size()));
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
