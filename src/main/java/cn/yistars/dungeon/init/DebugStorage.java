package cn.yistars.dungeon.init;

import cn.yistars.dungeon.BingDungeon;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.util.HashSet;

@Setter @Getter
public class DebugStorage {
    private CommandSender sender;
    private long startTime = System.currentTimeMillis();

    public World world;

    public int initialGridCount; // 初始网格点数量
    public int rectangleRemovedGridCount; // 移除矩形后网格点数量
    public int removedAGridCount; // 移除一个网格点后网格点数量
    public int reachableGridCount; // 可达网格点数量

    public final HashSet<Rectangle> rectangleRemoved = new HashSet<>();

    public DebugStorage(CommandSender sender) {
        this.sender = sender;
    }

    public DebugStorage() {}

    public void finish() {
        if (sender == null) return;

        sender.sendMessage("移除的矩形(不包含边缘)个数: " + rectangleRemoved.size());

        // 输出用时
        sender.sendMessage("用时: " + (System.currentTimeMillis() - startTime) + " 毫秒");
    }

    public void setWorld(World world) {
        this.world = world;

        if (sender == null) return;

        if (sender instanceof Player player) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location location = world.getSpawnLocation();
                    location.setY(150);
                    location.setPitch(90);
                    player.teleport(location);
                }
            }.runTask(BingDungeon.instance);
        }

        sender.sendMessage("");
        sender.sendMessage(" §e§lDEBUG INFORMATION 调试信息");
        sender.sendMessage(" §7世界: " + world.getName());
        sender.sendMessage("");
    }

    public void setInitialGridCount(int count) {
        this.initialGridCount = count;

        if (sender != null) sender.sendMessage("初始网格点数量: " + count);
    }

    public void setRectangleRemovedGridCount(int count) {
        this.rectangleRemovedGridCount = count;

        if (sender != null) sender.sendMessage("移除矩形后网格点数量: " + count);
    }

    public void setRemovedAGridCount(int count) {
        this.removedAGridCount = count;

        if (sender != null) sender.sendMessage("移除一个网格点后网格点数量: " + count);
    }

    public void setReachableGridCount(int count) {
        this.reachableGridCount = count;

        if (sender != null) sender.sendMessage("可达网格点数量: " + count);
    }
}
