package cn.yistars.dungeon.setup;

import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class SetupManager {
    public static HashMap<UUID, SetupPlayer> setupPlayers = new HashMap<>();

    public static void addSetupPlayer(Player player) {
        setupPlayers.put(player.getUniqueId(), new SetupPlayer(player));
    }

    public static void removeSetupPlayer(Player player) {
        if (!setupPlayers.containsKey(player.getUniqueId())) return;
        setupPlayers.get(player.getUniqueId()).cancel();
    }

    public static SetupPlayer getSetupPlayer(Player player) {
        return setupPlayers.get(player.getUniqueId());
    }

    public static Boolean isSetupStick(ItemStack item) {
        return NBT.get(item, nbt -> {
            if (!nbt.hasTag("BingDungeon")) return false;
            return nbt.getString("BingDungeon").equals("setup-stick");
        });
    }
}
