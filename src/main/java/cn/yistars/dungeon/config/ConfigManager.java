package cn.yistars.dungeon.config;

import cn.yistars.dungeon.BingDungeon;
import cn.yistars.dungeon.arena.ArenaManager;
import cn.yistars.dungeon.road.RoadManager;

public class ConfigManager {
    public static void loadConfig() {
        BingDungeon.instance.saveDefaultConfig();
        BingDungeon.instance.reloadConfig();
        BingDungeon.instance.Lang.saveDefaultConfig();
        BingDungeon.instance.Lang.reloadConfig();
        BingDungeon.instance.Rooms.saveDefaultConfig();
        BingDungeon.instance.Rooms.reloadConfig();
        BingDungeon.instance.Roads.saveDefaultConfig();
        BingDungeon.instance.Roads.reloadConfig();

        // 如果插件目录缺少文件夹则创建
        BingDungeon.instance.getDataFolder().toPath().resolve("rooms").toFile().mkdir();
        BingDungeon.instance.getDataFolder().toPath().resolve("roads").toFile().mkdir();

        ArenaManager.initArena();

        RoadManager.initRoadFiles();
    }
}
