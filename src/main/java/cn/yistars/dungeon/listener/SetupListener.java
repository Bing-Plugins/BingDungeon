package cn.yistars.dungeon.listener;

import cn.yistars.dungeon.setup.room.SetupRoomManager;
import cn.yistars.dungeon.setup.room.SetupRoomPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class SetupListener implements Listener {
    /*
    当玩家离开时移除配置状态
     */
    @EventHandler
    public void onPlayerQuitServer(PlayerQuitEvent event) {
        if (!SetupRoomManager.setupPlayers.containsKey(event.getPlayer().getUniqueId())) return;
        SetupRoomManager.removeSetupPlayer(event.getPlayer());
    }

    @EventHandler
    public void onSetupFirstLocation(BlockBreakEvent event) {
        if (!SetupRoomManager.setupPlayers.containsKey(event.getPlayer().getUniqueId())) return;
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();

        if (!SetupRoomManager.isSetupRegionStick(item)) return;

        SetupRoomPlayer setupRoomPlayer = SetupRoomManager.setupPlayers.get(event.getPlayer().getUniqueId());

        setupRoomPlayer.setFirstLocation(event.getBlock().getLocation());

        setupRoomPlayer.getSetupTip().sendRegionTip();

        event.setCancelled(true);
    }

    @EventHandler
    public void onSetupSecondLocation(PlayerInteractEvent event) {
        if (!SetupRoomManager.setupPlayers.containsKey(event.getPlayer().getUniqueId())) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();

        if (!SetupRoomManager.isSetupRegionStick(item)) return;

        SetupRoomPlayer setupRoomPlayer = SetupRoomManager.setupPlayers.get(event.getPlayer().getUniqueId());

        setupRoomPlayer.setSecondLocation(event.getClickedBlock().getLocation());

        setupRoomPlayer.getSetupTip().sendRegionTip();

        event.setCancelled(true);
    }

    @EventHandler
    public void onSetupDoors(PlayerInteractEvent event) {
        if (!SetupRoomManager.setupPlayers.containsKey(event.getPlayer().getUniqueId())) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();

        if (!SetupRoomManager.isSetupDoorsStick(item)) return;

        SetupRoomPlayer setupRoomPlayer = SetupRoomManager.setupPlayers.get(event.getPlayer().getUniqueId());

        if (setupRoomPlayer.addDoor(event.getClickedBlock().getLocation())) {
            setupRoomPlayer.getSetupTip().sendDoorsTip();
        }

        event.setCancelled(true);
    }
}
