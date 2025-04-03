package cn.yistars.dungeon.init;

import cn.yistars.dungeon.room.Room;
import cn.yistars.dungeon.room.door.Door;
import cn.yistars.dungeon.room.door.DoorType;

import java.awt.*;
import java.util.List;

public class RectangleSeparator {
    private final List<Room> rooms;
    // 最大尝试对齐次数
    private final int MAX_ALIGN_ATTEMPTS = 50;
    // 最大允许的门之间的偏移量
    private final int MAX_DOOR_OFFSET = 2;

    public RectangleSeparator(List<Room> rooms) {
        this.rooms = rooms;
    }

    public void separate() {
        // 第一步：分离所有矩形，避免重叠
        separateRectangles();
        
        // 第二步：尝试对齐门
        alignDoors();
    }
    
    private void separateRectangles() {
        boolean anyOverlap;
        do {
            anyOverlap = false;
            for (int i = 0; i < rooms.size(); i++) {
                Point force = new Point(0, 0);
                int overlapCounter = 0;
                for (int j = 0; j < rooms.size(); j++) {
                    if (i == j) continue;
                    if (!isOverlapped(rooms.get(i), rooms.get(j))) continue;
                    force.x += rooms.get(j).getMarginRectangle().x - rooms.get(i).getMarginRectangle().x;
                    force.y += rooms.get(j).getMarginRectangle().y - rooms.get(i).getMarginRectangle().y;
                    overlapCounter++;
                }
                if (overlapCounter == 0) continue;
                force.x /= overlapCounter;
                force.y /= overlapCounter;
                force.x *= -1;
                force.y *= -1;
                moveRectangle(rooms.get(i), force);
                anyOverlap = true;
            }
        } while (anyOverlap);
    }
    
    private void alignDoors() {
        // 尝试对齐门
        for (int attempt = 0; attempt < MAX_ALIGN_ATTEMPTS; attempt++) {
            boolean anyDoorAligned = false;
            
            for (int i = 0; i < rooms.size(); i++) {
                Room room1 = rooms.get(i);
                
                for (Door door1 : room1.getDoors()) {
                    for (int j = 0; j < rooms.size(); j++) {
                        if (i == j) continue;
                        Room room2 = rooms.get(j);
                        
                        for (Door door2 : room2.getDoors()) {
                            // 只考虑可能连接的门对（方向相反）
                            if (door1.getType() != door2.getType().getOpposite()) continue;
                            
                            // 计算这两个门的世界坐标
                            Point p1 = getWorldDoorCoord(room1, door1);
                            Point p2 = getWorldDoorCoord(room2, door2);
                            
                            // 检查门是否可以对齐
                            if (canAlign(door1, door2, p1, p2)) {
                                // 计算需要移动的距离
                                Point force = calculateAlignmentForce(door1, door2, p1, p2);
                                
                                if (force.x != 0 || force.y != 0) {
                                    // 尝试移动房间以对齐门
                                    Point originalPos1 = new Point(room1.getMarginRectangle().x, room1.getMarginRectangle().y);
                                    Point originalPos2 = new Point(room2.getMarginRectangle().x, room2.getMarginRectangle().y);
                                    
                                    // 按比例将力分配给两个房间
                                    moveRectangle(room1, new Point(-force.x/2, -force.y/2));
                                    moveRectangle(room2, new Point(force.x/2, force.y/2));
                                    
                                    // 检查移动后是否有重叠
                                    boolean hasOverlap = checkAnyOverlap(room1) || checkAnyOverlap(room2);
                                    
                                    if (hasOverlap) {
                                        // 如果移动导致重叠，恢复原始位置
                                        room1.setPosition(originalPos1.x, originalPos1.y);
                                        room2.setPosition(originalPos2.x, originalPos2.y);
                                    } else {
                                        anyDoorAligned = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // 如果没有任何门被对齐，退出循环
            if (!anyDoorAligned) {
                break;
            }
        }
    }
    
    private boolean canAlign(Door door1, Door door2, Point p1, Point p2) {
        // 根据门的类型检查是否可以对齐
        if (door1.getType() == DoorType.NORTH || door1.getType() == DoorType.SOUTH) {
            // 垂直方向的门需要x坐标接近
            return Math.abs(p1.x - p2.x) <= MAX_DOOR_OFFSET;
        } else {
            // 水平方向的门需要y坐标接近
            return Math.abs(p1.y - p2.y) <= MAX_DOOR_OFFSET;
        }
    }
    
    private Point calculateAlignmentForce(Door door1, Door door2, Point p1, Point p2) {
        Point force = new Point(0, 0);
        
        if (door1.getType() == DoorType.NORTH || door1.getType() == DoorType.SOUTH) {
            // 垂直方向的门，调整x坐标
            force.x = p1.x - p2.x;
        } else {
            // 水平方向的门，调整y坐标
            force.y = p1.y - p2.y;
        }
        
        return force;
    }
    
    private Point getWorldDoorCoord(Room room, Door door) {
        // 将门的坐标转换为世界坐标
        return new Point(room.getMarginRectangle().x + door.getX(), room.getMarginRectangle().y + door.getZ());
    }
    
    private boolean checkAnyOverlap(Room room) {
        for (Room otherRoom : rooms) {
            if (room != otherRoom && isOverlapped(room, otherRoom)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOverlapped(Room r1, Room r2) {
        return r1.getMarginRectangle().intersects(r2.getMarginRectangle());
    }

    private void moveRectangle(Room room, Point move) {
        room.setPosition(room.getMarginRectangle().x + move.x, room.getMarginRectangle().y + move.y);
    }
}
