package cn.yistars.dungeon.road;

import lombok.Getter;

@Getter
public class Road {
    private final Boolean north, south, east, west;
    private final RoadType roadType;

    public Road(boolean north, boolean south, boolean east, boolean west, RoadType roadType) {
        this.north = north;
        this.south = south;
        this.east = east;
        this.west = west;
        this.roadType = roadType;
    }
}