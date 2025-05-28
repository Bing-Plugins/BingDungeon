package cn.yistars.dungeon.init.grid;

import lombok.Getter;

import java.awt.*;

@Getter
public class GridPoint {
    private final Point point;
    private final PointType type;

    public GridPoint(Point point, PointType type) {
        this.point = point;
        this.type = type;
    }
}
