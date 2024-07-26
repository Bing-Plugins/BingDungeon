package cn.yistars.dungeon.config;

import cn.yistars.dungeon.BingDungeon;

public class ConfigManager {
    public static void loadConfig() {
        BingDungeon.instance.saveDefaultConfig();
        BingDungeon.instance.reloadConfig();
        BingDungeon.instance.Lang.saveDefaultConfig();
        BingDungeon.instance.Lang.reloadConfig();
    }
}
