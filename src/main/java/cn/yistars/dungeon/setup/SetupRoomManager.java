package cn.yistars.dungeon.setup;

import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class SetupRoomManager {
    public static HashMap<UUID, SetupRoomPlayer> setupPlayers = new HashMap<>();

    public static void addSetupPlayer(Player player) {
        setupPlayers.put(player.getUniqueId(), new SetupRoomPlayer(player));
    }

    public static void removeSetupPlayer(Player player) {
        if (!setupPlayers.containsKey(player.getUniqueId())) return;
        setupPlayers.get(player.getUniqueId()).cancel();
    }

    public static SetupRoomPlayer getSetupPlayer(Player player) {
        return setupPlayers.get(player.getUniqueId());
    }

    public static Boolean isSetupRegionStick(ItemStack item) {
        return NBT.get(item, nbt -> {
            if (!nbt.hasTag("BingDungeon")) return false;
            return nbt.getString("BingDungeon").equals("setup-region-stick");
        });
    }

    public static Boolean isSetupDoorsStick(ItemStack item) {
        return NBT.get(item, nbt -> {
            if (!nbt.hasTag("BingDungeon")) return false;
            return nbt.getString("BingDungeon").equals("setup-doors-stick");
        });
    }
}
