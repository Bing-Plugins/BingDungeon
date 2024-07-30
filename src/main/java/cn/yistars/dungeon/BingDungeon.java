package cn.yistars.dungeon;

import cn.yistars.dungeon.command.MainCommand;
import cn.yistars.dungeon.config.ConfigAccessor;
import cn.yistars.dungeon.config.ConfigManager;
import cn.yistars.dungeon.listener.PlayerListener;
import cn.yistars.dungeon.listener.SetupListener;
import org.bukkit.plugin.java.JavaPlugin;

public class BingDungeon extends JavaPlugin {
    public static BingDungeon instance;
    public final ConfigAccessor Lang = new ConfigAccessor(this, "Lang.yml");
    public final ConfigAccessor Rooms = new ConfigAccessor(this, "Rooms.yml");

    @Override
    public void onEnable() {
        instance = this;

        ConfigManager.loadConfig();

        this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        this.getCommand("BingDungeon").setExecutor(new MainCommand());

        if (getConfig().getBoolean("setup-mode", false)) {
            this.getServer().getPluginManager().registerEvents(new SetupListener(), this);
        }

        this.getLogger().info("Enabled successfully.");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Disabled successfully.");
    }
}
