package cn.yistars.dungeon;

import cn.yistars.dungeon.arena.Arena;
import cn.yistars.dungeon.command.MainCommand;
import cn.yistars.dungeon.config.ConfigAccessor;
import cn.yistars.dungeon.config.ConfigManager;
import cn.yistars.dungeon.listener.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class BingDungeon extends JavaPlugin {
    public static BingDungeon instance;
    public final ConfigAccessor Lang = new ConfigAccessor(this, "Lang.yml");

    @Override
    public void onEnable() {
        instance = this;

        ConfigManager.loadConfig();

        this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        this.getCommand("BingDungeon").setExecutor(new MainCommand());

        this.getLogger().info("Enabled successfully.");

        new BukkitRunnable() {
            @Override
            public void run() {
                new Arena();
            }
        }.runTaskAsynchronously(this);
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Disabled successfully.");
    }
}
