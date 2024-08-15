package cn.yistars.dungeon.config;

import cn.yistars.dungeon.BingDungeon;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class LangManager {
    public static void sendMsg(Player player, String key, String... args) {
        String msg = BingDungeon.instance.Lang.getConfig().getString(key);

        if (msg == null || msg.isEmpty()) return;

        msg = ChatColor.translateAlternateColorCodes('&', msg);
        player.sendMessage(msg);
    }

    public static String getLang(String key, String... args) {
        String msg = BingDungeon.instance.Lang.getConfig().getString(key);

        if (msg == null) return "";

        switch(key) {
            case "setup-id-status":
            case "setup-save-success-msg":
            case "setup-door-id-status":
                msg = msg.replace("%id%", args[0]);
                break;
            case "setup-type-status":
                msg = msg.replace("%type%", args[0]);
                break;
            case "setup-first-location-status":
                msg = msg.replace("%first-location%", args[0]);
                break;
            case "setup-second-location-status":
                msg = msg.replace("%second-location%", args[0]);
                break;
            case "setup-location-format":
                msg = msg.replace("%world%", args[0]);
                msg = msg.replace("%x%", args[1]);
                msg = msg.replace("%y%", args[2]);
                msg = msg.replace("%z%", args[3]);
                break;
            case "setup-location-hover-size":
                msg = msg.replace("%length%", args[0]);
                msg = msg.replace("%width%", args[1]);
                msg = msg.replace("%height%", args[2]);
                break;
            case "setup-location-hover-not-allow-size":
                msg = msg.replace("%unit-size%", args[0]);
                break;
            case "setup-door-status":
                msg = msg.replace("%num%", args[0]);
                break;
            case "setup-door-location-format":
                msg = msg.replace("%x%", args[0]);
                msg = msg.replace("%y%", args[1]);
                msg = msg.replace("%z%", args[2]);
                break;
            case "setup-door-location-hover":
                msg = msg.replace("%x%", args[0]);
                msg = msg.replace("%z%", args[1]);
                msg = msg.replace("%y-offset%", args[2]);
                msg = msg.replace("%facing%", args[3]);
                break;
        }

        msg = ChatColor.translateAlternateColorCodes('&', msg);
        return msg;
    }

    public static String getNumberStr(String money) {
        BigDecimal bd = new BigDecimal(money);
        return bd.stripTrailingZeros().toPlainString();
    }
}
