package cn.yistars.dungeon.listener;

import cn.yistars.dungeon.arena.Arena;
import cn.yistars.dungeon.arena.ArenaManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.MapInitializeEvent;

public class PlayerListener implements Listener {
    @EventHandler
    public void onMapInitializeEvent(MapInitializeEvent event) {
        Arena findArena = null;
        for (Arena arena : ArenaManager.arenas) {
            if (!event.getMap().getWorld().equals(arena.getWorld())) continue;
            findArena = arena;
            break;
        }
        
        if (findArena == null) return;
        
        findArena.getArenaMap().updateMapView(event.getMap());
    }
}
