package cn.yistars.dungeon.listener;

import cn.yistars.dungeon.setup.SetupManager;
import cn.yistars.dungeon.setup.SetupPlayer;
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
        if (!SetupManager.setupPlayers.containsKey(event.getPlayer().getUniqueId())) return;
        SetupManager.removeSetupPlayer(event.getPlayer());
    }

    @EventHandler
    public void onSetupFirstLocation(BlockBreakEvent event) {
        if (!SetupManager.setupPlayers.containsKey(event.getPlayer().getUniqueId())) return;
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();

        if (!SetupManager.isSetupRegionStick(item)) return;

        SetupPlayer setupPlayer = SetupManager.setupPlayers.get(event.getPlayer().getUniqueId());

        setupPlayer.setFirstLocation(event.getBlock().getLocation());

        setupPlayer.getSetupTip().sendRegionTip();

        event.setCancelled(true);
    }

    @EventHandler
    public void onSetupSecondLocation(PlayerInteractEvent event) {
        if (!SetupManager.setupPlayers.containsKey(event.getPlayer().getUniqueId())) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();

        if (!SetupManager.isSetupRegionStick(item)) return;

        SetupPlayer setupPlayer = SetupManager.setupPlayers.get(event.getPlayer().getUniqueId());

        setupPlayer.setSecondLocation(event.getClickedBlock().getLocation());

        setupPlayer.getSetupTip().sendRegionTip();

        event.setCancelled(true);
    }

    @EventHandler
    public void onSetupDoors(PlayerInteractEvent event) {
        if (!SetupManager.setupPlayers.containsKey(event.getPlayer().getUniqueId())) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();

        if (!SetupManager.isSetupDoorsStick(item)) return;

        SetupPlayer setupPlayer = SetupManager.setupPlayers.get(event.getPlayer().getUniqueId());

        if (setupPlayer.addDoor(event.getClickedBlock().getLocation())) {
            setupPlayer.getSetupTip().sendDoorsTip();
        }

        event.setCancelled(true);
    }
}
