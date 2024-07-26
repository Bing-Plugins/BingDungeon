package cn.yistars.dungeon.config;

import cn.yistars.dungeon.BingDungeon;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class LangManager {
    public static void sendMsg(Player player, String key, String... args) {
        String msg = BingDungeon.instance.Lang.getConfig().getString(key);

        if (msg == null || msg.equals("")) return;

        msg = ChatColor.translateAlternateColorCodes('&', msg);
        player.sendMessage(msg);
    }

    public static String getLang(String key, String... args) {
        String msg = BingDungeon.instance.Lang.getConfig().getString(key);

        if (msg == null) return "";

        msg = ChatColor.translateAlternateColorCodes('&', msg);
        return msg;
    }

    public static String getNumberStr(String money) {
        BigDecimal bd = new BigDecimal(money);
        return bd.stripTrailingZeros().toPlainString();
    }
}
