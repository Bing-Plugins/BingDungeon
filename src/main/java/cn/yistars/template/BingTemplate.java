package cn.yistars.template;

import cn.yistars.template.command.MainCommand;
import cn.yistars.template.config.ConfigAccessor;
import cn.yistars.template.config.ConfigManager;
import cn.yistars.template.listener.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

public class BingTemplate extends JavaPlugin {
    public static BingTemplate instance;
    public final ConfigAccessor Lang = new ConfigAccessor(this, "Lang.yml");

    @Override
    public void onEnable() {
        instance = this;

        ConfigManager.loadConfig();

        this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        this.getCommand("BingTemplate").setExecutor(new MainCommand());

        this.getLogger().info("Enabled successfully.");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Disabled successfully.");
    }
}
