package cn.yistars.dungeon.command;

import cn.yistars.dungeon.BingDungeon;
import cn.yistars.dungeon.arena.ArenaManager;
import cn.yistars.dungeon.config.ConfigManager;
import cn.yistars.dungeon.config.LangManager;
import cn.yistars.dungeon.room.RoomType;
import cn.yistars.dungeon.setup.SetupManager;
import cn.yistars.dungeon.setup.SetupPlayer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command commands, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("BingDungeon v%version% by Bing_Yanchi".replace("%version%", BingDungeon.instance.getDescription().getVersion()));

            if (sender.hasPermission("BingDungeon.admin")) {
                sender.sendMessage(LangManager.getLang("main-get-help"));
            }

            return true;
        }

        if (!sender.hasPermission("BingDungeon.admin")) {
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                for (String line : LangManager.getLang("main-command-help").split("\n")) {
                    sender.sendMessage(line);
                }
                return true;
            case "reload":
                ConfigManager.loadConfig();

                sender.sendMessage(LangManager.getLang("success-reload"));

                return true;
            case "setup":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(LangManager.getLang("need-player"));
                    return false;
                }

                Player player = (Player) sender;
                if (!BingDungeon.instance.getConfig().getBoolean("setup-mode")) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(LangManager.getLang("setup-not-enable-setup")));
                    return false;
                }

                SetupPlayer setupPlayer = SetupManager.getSetupPlayer(player);

                if (args.length == 1) {
                    if (setupPlayer == null) {
                        SetupManager.addSetupPlayer(player);
                    } else {
                        setupPlayer.getSetupTip().sendTip();
                    }
                    return true;
                } else {
                    if (setupPlayer == null) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(LangManager.getLang("setup-player-not-in-setup")));
                        return false;
                    }

                    switch (args[1].toLowerCase()) {
                        case "complete":
                            setupPlayer.complete();
                            return true;
                        case "id":
                            if (args.length < 3) return false;
                            setupPlayer.setId(args[2]);
                            setupPlayer.getSetupTip().sendTip();
                            return true;
                        case "type":
                            if (args.length < 3) return false;
                            setupPlayer.setRoomType(RoomType.valueOf(args[2].toUpperCase()));
                            setupPlayer.getSetupTip().sendTip();
                            return true;
                        case "cancel":
                            SetupManager.setupPlayers.remove(player.getUniqueId());
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(LangManager.getLang("setup-cancel")));
                            LangManager.sendMsg(player, "setup-cancel-msg");
                            return true;
                    }
                    return false;
                }
            case "debug":
                ArenaManager.createArena();
                return true;
            default:
                return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("BingDungeon.admin")) {
            return completions;
        }

        switch (args.length) {
            case 1:
                // 定义列表
                String[] Commands = {"reload", "help", "setup"};
                // 通过开头判断
                StringUtil.copyPartialMatches(args[0], Arrays.asList(Commands), completions);
                break;
            case 2:
                switch (args[0].toLowerCase()) {
                    case "setup":
                        String[] SetupCommands = {"complete", "id", "type", "cancel"};
                        StringUtil.copyPartialMatches(args[1], Arrays.asList(SetupCommands), completions);
                        break;
                }
                break;
            case 3:
                switch (args[1].toLowerCase()) {
                    case "type":
                        ArrayList<String> roomTypes = new ArrayList<>();
                        for (RoomType roomType : RoomType.values()) {
                            roomTypes.add(roomType.toString().toLowerCase());
                        }
                        StringUtil.copyPartialMatches(args[2], roomTypes, completions);
                        break;
                }
        }

        // 排序
        Collections.sort(completions);
        // 返回
        return completions;
    }
}
