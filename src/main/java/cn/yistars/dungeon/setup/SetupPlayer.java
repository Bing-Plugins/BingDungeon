package cn.yistars.dungeon.setup;

import cn.yistars.dungeon.BingDungeon;
import cn.yistars.dungeon.config.LangManager;
import cn.yistars.dungeon.room.RoomType;
import cn.yistars.dungeon.room.door.Door;
import cn.yistars.dungeon.room.door.DoorType;
import cn.yistars.dungeon.setup.tip.SetupTip;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import de.tr7zw.changeme.nbtapi.NBT;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;

@Getter
public class SetupPlayer {
    private final Player player;
    private final SetupTip setupTip;
    @Setter
    private String id;
    @Setter
    private RoomType roomType;
    private Location firstLocation, secondLocation;
    private CuboidRegion region;
    private final HashSet<Door> doors = new HashSet<>();
    private Integer yOffset = -1;

    public SetupPlayer(Player player) {
        this.player = player;
        this.setupTip = new SetupTip(this);

        giveItem();

        this.setupTip.sendTip();
    }

    private void giveItem() {
        player.getInventory().addItem(getItem());
    }

    private ItemStack getItem() {
        ItemStack itemStack = new ItemStack(Material.STICK);
        NBT.modify(itemStack, nbt -> {
            nbt.setString("BingDungeon", "setup-stick");
        });

        return itemStack;
    }

    public void setFirstLocation(Location location) {
        this.firstLocation = location;
        if (secondLocation != null) {
            this.region = new CuboidRegion(BukkitAdapter.asBlockVector(firstLocation), BukkitAdapter.asBlockVector(secondLocation));
        }
    }

    public void setSecondLocation(Location location) {
        this.secondLocation = location;
        if (firstLocation != null) {
            this.region = new CuboidRegion(BukkitAdapter.asBlockVector(firstLocation), BukkitAdapter.asBlockVector(secondLocation));
        }
    }

    public void setDoor(Location location) {
        // 如果不在选区内则返回
        if (!region.contains(BukkitAdapter.asBlockVector(location))) return;

        // 当坐标不在边上的时候直接返回
        if (location.getBlockX() != firstLocation.getBlockX() && location.getBlockX() != secondLocation.getBlockX()) return;
        if (location.getBlockZ() != firstLocation.getBlockZ() && location.getBlockZ() != secondLocation.getBlockZ()) return;

        // 计算可用门位置
        BlockVector3 minBlock = region.getMinimumPoint();
        BlockVector3 maxBlock = region.getMaximumPoint();

        int unit = BingDungeon.instance.getConfig().getInt("unit-size");
        int halfUnit = unit / 2 + 1;

        int doorX = 1;
        boolean isFind = false;
        for (int i = halfUnit; i < region.getMaximumX() - region.getMinimumX(); i += unit) {
            if (location.getBlockX() == minBlock.x() + i) {
                isFind = true;
                break;
            }
            doorX += 1;
        }
        if (!isFind) return;

        int doorZ = 1;
        isFind = false;
        for (int i = halfUnit; i < region.getMaximumZ() - region.getMinimumZ(); i += unit) {
            if (location.getBlockZ() == minBlock.z() + i) {
                isFind = true;
                break;
            }
            doorZ += 1;
        }
        if (!isFind) return;

        // 设置高度偏移量或判断偏移量是否一致
        if (doors.isEmpty()) {
            yOffset = location.getBlockY() - region.getMinimumPoint().y();
        } else {
            if (location.getBlockY() - region.getMinimumPoint().y() != yOffset) return;
        }

        // 判断面向位置
        DoorType doorType;
        if (location.getBlockX() == region.getMinimumX()) {
            doorType = DoorType.SOUTH;
        } else if (location.getBlockX() == region.getMaximumX()) {
            doorType = DoorType.NORTH;
        } else if (location.getBlockZ() == region.getMinimumZ()) {
            doorType = DoorType.WEST;
        } else {
            doorType = DoorType.EAST;
        }

        doors.add(new Door(doorType, doorX, doorZ));
    }

    public boolean isLengthAllowed() {
        if (firstLocation == null || secondLocation == null) return true;
        return region.getLength() % BingDungeon.instance.getConfig().getInt("unit-size") == 0;
    }

    public boolean isWidthAllowed() {
        if (firstLocation == null || secondLocation == null) return true;
        return region.getWidth() % BingDungeon.instance.getConfig().getInt("unit-size") == 0;
    }

    public void cancel() {
        SetupManager.setupPlayers.remove(player.getUniqueId());
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack == null) continue;
            if (SetupManager.isSetupStick(itemStack)) {
                player.getInventory().remove(itemStack);
            }
        }
    }

    public boolean canComplete() {
        return firstLocation != null && secondLocation != null && roomType != null && id != null && firstLocation.getWorld().equals(secondLocation.getWorld()) && isAllowSize();
    }

    public boolean isSameWorld() {
        if (firstLocation == null || secondLocation == null) return true;
        return firstLocation.getWorld().equals(secondLocation.getWorld());
    }

    public boolean isAllowSize() {
        if (region == null) return true;
        // 判断 region 的宽高是否都整除 7
        return region.getWidth() % BingDungeon.instance.getConfig().getInt("unit-size") == 0 && region.getLength() % BingDungeon.instance.getConfig().getInt("unit-size") == 0;
    }

    public void complete() {
        // 检查是否都不为空
        if (!canComplete()) {
            this.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(LangManager.getLang("setup-still-unfinished-projects")));
            return;
        }

        if (!firstLocation.getWorld().equals(secondLocation.getWorld())) {
            this.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(LangManager.getLang("setup-location-not-in-same-world")));
            return;
        }

        CuboidRegion region = new CuboidRegion(BukkitAdapter.asBlockVector(firstLocation), BukkitAdapter.asBlockVector(secondLocation));

        // 判断 region 的宽高是否都整除 7
        if (region.getWidth() % BingDungeon.instance.getConfig().getInt("unit-size") != 0 || region.getLength() % BingDungeon.instance.getConfig().getInt("unit-size") != 0) {
            this.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(LangManager.getLang("setup-room-unit-size-incorrect")));
            return;
        }

        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                BukkitAdapter.adapt(firstLocation.getWorld()), region, clipboard, region.getMinimumPoint()
        );
        forwardExtentCopy.setCopyingEntities(true);

        Operations.complete(forwardExtentCopy);

        // 保存
        File file = BingDungeon.instance.getDataFolder().toPath().resolve("rooms/" + id + ".schem").toFile();

        try (ClipboardWriter writer = BuiltInClipboardFormat.FAST.getWriter(Files.newOutputStream(file.toPath()))) {
            writer.write(clipboard);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 保存到配置文件
        BingDungeon.instance.Rooms.getConfig().set(id + ".width", region.getWidth() / BingDungeon.instance.getConfig().getInt("unit-size"));
        BingDungeon.instance.Rooms.getConfig().set(id + ".length", region.getLength() / BingDungeon.instance.getConfig().getInt("unit-size"));
        BingDungeon.instance.Rooms.getConfig().set(id + ".unit", BingDungeon.instance.getConfig().getInt("unit-size"));
        BingDungeon.instance.Rooms.getConfig().set(id + ".type", roomType.toString());
        BingDungeon.instance.Rooms.saveConfig();

        // 保存提示
        TextComponent textComponent = new TextComponent(LangManager.getLang("setup-save-success-msg", id));
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bingdungeon setup"));
        player.spigot().sendMessage(textComponent);

        cancel();
    }
}
