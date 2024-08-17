package cn.yistars.dungeon.room.door;

public enum DoorType {
    NORTH,
    SOUTH,
    EAST,
    WEST;

    public DoorType getOpposite() {
        switch (this) {
            case NORTH:
                return SOUTH;
            case SOUTH:
                return NORTH;
            case EAST:
                return WEST;
            case WEST:
                return EAST;
            default:
                throw new IllegalArgumentException("Unknown DoorType: " + this);
        }
    }
}
