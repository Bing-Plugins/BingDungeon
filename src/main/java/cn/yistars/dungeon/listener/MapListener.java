package cn.yistars.dungeon.listener;

import cn.yistars.dungeon.arena.Arena;
import cn.yistars.dungeon.arena.ArenaManager;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Objects;

public class MapListener implements Listener {
    @EventHandler
    public void onFindArea(PlayerMoveEvent event) {
        Arena arena = ArenaManager.getArena(event.getPlayer());
        if (arena == null) return;

        Location location = event.getPlayer().getLocation();
        if (!Objects.equals(location.getWorld(), arena.getWorld())) return;

        arena.findArea(location);
    }
}
