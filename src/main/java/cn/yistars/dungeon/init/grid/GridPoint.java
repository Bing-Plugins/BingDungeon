package cn.yistars.dungeon.init.grid;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
public class GridPoint {
    private final Point point;
    @Setter
    private PointType type;

    public GridPoint(Point point, PointType type) {
        this.point = point;
        this.type = type;
    }
}
