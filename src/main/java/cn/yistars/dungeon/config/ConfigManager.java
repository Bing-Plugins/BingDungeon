package cn.yistars.dungeon.config;

import cn.yistars.dungeon.BingDungeon;
import cn.yistars.dungeon.arena.ArenaManager;

public class ConfigManager {
    public static void loadConfig() {
        BingDungeon.instance.saveDefaultConfig();
        BingDungeon.instance.reloadConfig();
        BingDungeon.instance.Lang.saveDefaultConfig();
        BingDungeon.instance.Lang.reloadConfig();
        BingDungeon.instance.Rooms.saveDefaultConfig();
        BingDungeon.instance.Rooms.reloadConfig();

        // 如果插件目录缺少 rooms 文件夹则创建
        BingDungeon.instance.getDataFolder().toPath().resolve("rooms").toFile().mkdir();

        ArenaManager.initArena();
    }
}
