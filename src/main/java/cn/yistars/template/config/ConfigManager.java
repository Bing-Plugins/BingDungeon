package cn.yistars.template.config;

import cn.yistars.template.BingTemplate;

public class ConfigManager {
    public static void loadConfig() {
        BingTemplate.instance.saveDefaultConfig();
        BingTemplate.instance.reloadConfig();
        BingTemplate.instance.Lang.saveDefaultConfig();
        BingTemplate.instance.Lang.reloadConfig();
    }
}
