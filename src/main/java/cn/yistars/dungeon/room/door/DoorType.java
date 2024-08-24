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

    public DoorType rotate(int angle) {
        switch (angle) {
            case 90:
                return switch (this) {
                    case NORTH -> EAST;
                    case SOUTH -> WEST;
                    case EAST -> SOUTH;
                    case WEST -> NORTH;
                };
            case 180:
                return switch (this) {
                    case NORTH -> SOUTH;
                    case SOUTH -> NORTH;
                    case EAST -> WEST;
                    case WEST -> EAST;
                };
            case 270:
                return switch (this) {
                    case NORTH -> WEST;
                    case SOUTH -> EAST;
                    case EAST -> NORTH;
                    case WEST -> SOUTH;
            };
            default:
                return this;
        }
    }
}
