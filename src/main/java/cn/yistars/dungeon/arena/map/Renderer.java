package cn.yistars.dungeon.arena.map;

import org.bukkit.entity.Player;
import org.bukkit.map.*;

public class Renderer extends MapRenderer {
    private final ArenaMap arenaMap;
    private MapCanvas mapCanvas;
    private Player player;

    public Renderer(ArenaMap arenaMap) {
        this.arenaMap = arenaMap;
    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
        this.mapCanvas = mapCanvas;
        this.player = player;
        updateMap(mapCanvas, player);
    }

    public void update() {
        updateMap(mapCanvas, player);
    }

    private void updateMap(MapCanvas mapCanvas, Player player) {
        // 地图大小 128 * 128，地图单元为 5
        mapCanvas.drawImage(0, 0, arenaMap.getBufferedImage());

        MapCursorCollection cursors = new MapCursorCollection();

        for (Player otherPlayer : arenaMap.getArena().getPlayers()) {
            if (otherPlayer.equals(player)) continue;
            MapCursor cursor = new MapCursor(
                    (byte) (otherPlayer.getLocation().getX() / 7 * 6),
                    (byte) (otherPlayer.getLocation().getZ() / 7 * 6),
                    getDirection(otherPlayer),
                    MapCursor.Type.BLUE_MARKER,
                    false,
                    otherPlayer.getName()
            );
            cursors.addCursor(cursor);
        }

        MapCursor cursor = new MapCursor(
                (byte) (player.getLocation().getX() / 7 * 6),
                (byte) (player.getLocation().getZ() / 7 * 6),
                getDirection(player),
                MapCursor.Type.PLAYER,
                true
        );
        cursors.addCursor(cursor);

        mapCanvas.setCursors(cursors);
    }

    private Byte getDirection(Player player) {
        float yaw = player.getLocation().getYaw();
        if (yaw < 0) {
            yaw += 360;
        }
        yaw %= 360;
        return (byte) ((int) ((yaw + 8) / 22.5) & 0xF);
    }
}