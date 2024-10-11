package cn.yistars.dungeon.arena.map;

import cn.yistars.dungeon.arena.Arena;
import cn.yistars.dungeon.road.Road;
import cn.yistars.dungeon.room.Room;
import cn.yistars.dungeon.room.door.DoorType;
import lombok.Getter;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;

@Getter
public class ArenaMap {
    private final Arena arena;
    private BufferedImage bufferedImage;
    private final HashSet<Renderer> renderers = new HashSet<>();

    public ArenaMap(Arena arena) {
        this.arena = arena;
    }

    public void update() {
        // 更新地图
        initBufferedImage();

        for (Renderer renderer : renderers) {
            renderer.update();
        }
    }

    public void updateMapView(MapView mapView) {
        mapView.setCenterX(0);
        mapView.setCenterZ(0);
        //mapView.setWorld(arena.getWorld());
        //mapView.setScale(MapView.Scale.NORMAL);
        mapView.setUnlimitedTracking(true);

        mapView.getRenderers().clear();
        for (MapRenderer renderer : mapView.getRenderers()) {
            mapView.removeRenderer(renderer);
        }

        Renderer renderer = new Renderer(this);
        mapView.addRenderer(renderer);
        renderers.add(renderer);
    }

    private void initBufferedImage() {
        bufferedImage = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = bufferedImage.createGraphics();

        for (Room room : arena.getRooms()) {
            if (!room.getIsFind()) continue;
            g2d.setColor(Color.ORANGE);
            for (int i = 0; i < room.getRectangle().height * 3; i++) {
                g2d.drawLine(64 + room.getRectangle().x * 3, 64 + room.getRectangle().y * 3 + i, 64 + room.getRectangle().x * 3 + room.getRectangle().width * 3, 64 + room.getRectangle().y * 3 + i);
            }

            g2d.setColor(Color.DARK_GRAY);
            g2d.drawRect(64 + room.getRectangle().x * 3, 64 + room.getRectangle().y * 3, room.getRectangle().width * 3, room.getRectangle().height * 3);
        }

        for (Road road : arena.getRoads()) {
            if (!road.getIsFind()) continue;
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawRect(64 + road.getRectangle().x * 3, 64 + road.getRectangle().y * 3, 3, 3);

            g2d.setColor(Color.RED);
            g2d.drawRect(64 + road.getRectangle().x * 3 + 1, 64 + road.getRectangle().y * 3 + 1, 1, 1);

            if (road.getFacings().contains(DoorType.NORTH)) {
                g2d.drawRect(64 + road.getRectangle().x * 3 + 1, 64 + road.getRectangle().y * 3 + 2, 1, 1);
            }
            if (road.getFacings().contains(DoorType.SOUTH)) {
                g2d.drawRect(64 + road.getRectangle().x * 3 + 1, 64 + road.getRectangle().y * 3, 1, 1);
            }
            if (road.getFacings().contains(DoorType.EAST)) {
                g2d.drawRect(64 + road.getRectangle().x * 3 + 2, 64 + road.getRectangle().y * 3 + 1, 1, 1);
            }
            if (road.getFacings().contains(DoorType.WEST)) {
                g2d.drawRect(64 + road.getRectangle().x * 3, 64 + road.getRectangle().y * 3 + 1, 1, 1);
            }
        }
    }
}
