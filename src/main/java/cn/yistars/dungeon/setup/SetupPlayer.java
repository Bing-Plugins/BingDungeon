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
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
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

        giveRegionItem();

        this.setupTip.sendRegionTip();
    }

    private void giveRegionItem() {
        player.getInventory().addItem(getRegionItem());
    }

    private void giveDoorsItem() {
        player.getInventory().addItem(getDoorsItem());
    }

    private ItemStack getRegionItem() {
        ItemStack itemStack = new ItemStack(Material.STICK);
        NBT.modify(itemStack, nbt -> {
            nbt.setString("BingDungeon", "setup-region-stick");
        });
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(LangManager.getLang("setup-region-stick-name"));
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    private ItemStack getDoorsItem() {
        ItemStack itemStack = new ItemStack(Material.BLAZE_ROD);
        NBT.modify(itemStack, nbt -> {
            nbt.setString("BingDungeon", "setup-doors-stick");
        });
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(LangManager.getLang("setup-doors-stick-name"));
        itemStack.setItemMeta(itemMeta);

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

    public boolean addDoor(Location location) {
        // 如果不在选区内则返回
        if (!region.contains(BukkitAdapter.asBlockVector(location))) {
            this.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(LangManager.getLang("setup-door-not-in-region")));
            return false;
        }

        // 当坐标不在边上的时候直接返回
        if (location.getBlockX() != region.getMinimumX() && location.getBlockX() != region.getMaximumX() && location.getBlockZ() != region.getMinimumZ() && location.getBlockZ() != region.getMaximumZ()) {
            this.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(LangManager.getLang("setup-door-not-in-edge")));
            return false;
        }

        // 设置高度偏移量或判断偏移量是否一致
        if (doors.isEmpty()) {
            yOffset = location.getBlockY() - region.getMinimumPoint().y();
        } else {
            if (location.getBlockY() - region.getMinimumPoint().y() != yOffset) {
                this.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(LangManager.getLang("setup-door-not-same-y-offset")));
                return false;
            }
        }

        // 判断面向位置
        DoorType doorType;
        if (location.getBlockZ() == region.getMinimumZ()) {
            doorType = DoorType.SOUTH;
        } else if (location.getBlockZ() == region.getMaximumZ()) {
            doorType = DoorType.NORTH;
        } else if (location.getBlockX() == region.getMinimumX()) {
            doorType = DoorType.WEST;
        } else {
            doorType = DoorType.EAST;
        }

        // 计算可用门位置
        int unit = BingDungeon.instance.getConfig().getInt("unit-size"); // 需要为奇数方便获取中间位置
        int halfUnit = unit / 2; // 获取中间位置

        int doorX = 0, doorZ = 0;
        boolean isFind = false;
        switch (doorType) {
            case NORTH:
                doorZ = region.getLength() / BingDungeon.instance.getConfig().getInt("unit-size") - 1;
            case SOUTH:
                for (int i = halfUnit; i < region.getMaximumX() - region.getMinimumX(); i += unit) {
                    if (location.getBlockX() == region.getMinimumX() + i) {
                        isFind = true;
                        break;
                    }
                    doorX += 1;
                }
                break;
            case EAST:
                doorX = region.getWidth() / BingDungeon.instance.getConfig().getInt("unit-size") - 1;
            case WEST:
                for (int i = halfUnit; i < region.getMaximumZ() - region.getMinimumZ(); i += unit) {
                    if (location.getBlockZ() == region.getMinimumZ() + i) {
                        isFind = true;
                        break;
                    }
                    doorZ += 1;
                }
                break;
        }

        if (!isFind) {
            this.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(LangManager.getLang("setup-door-not-in-center-of-unit")));
            return false;
        }

        // 检查重复
        int finalDoorX = doorX;
        int finalDoorZ = doorZ;
        if (doors.stream().anyMatch(door -> door.getX() == finalDoorX && door.getZ() == finalDoorZ)) {
            this.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(LangManager.getLang("setup-door-already-exist")));
            return false;
        }

        doors.add(new Door(doorType, doorX, doorZ, location));
        return true;
    }

    public void clearDoors() {
        doors.clear();
        yOffset = -1;
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
            if (SetupManager.isSetupRegionStick(itemStack)) {
                player.getInventory().remove(itemStack);
            }
            if (SetupManager.isSetupDoorsStick(itemStack)) {
                player.getInventory().remove(itemStack);
            }
        }
    }

    public boolean canSaveRegion() {
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

    public void completeRegion() {
        // 检查是否都不为空
        if (!canSaveRegion()) {
            this.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(LangManager.getLang("setup-still-unfinished-projects")));
            return;
        }

        if (!firstLocation.getWorld().equals(secondLocation.getWorld())) {
            this.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(LangManager.getLang("setup-location-not-in-same-world")));
            return;
        }

        region = new CuboidRegion(BukkitAdapter.asBlockVector(firstLocation), BukkitAdapter.asBlockVector(secondLocation));

        // 判断 region 的宽高是否都整除 7
        if (region.getWidth() % BingDungeon.instance.getConfig().getInt("unit-size") != 0 || region.getLength() % BingDungeon.instance.getConfig().getInt("unit-size") != 0) {
            this.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(LangManager.getLang("setup-room-unit-size-incorrect")));
            return;
        }

        giveDoorsItem();
        this.setupTip.sendDoorsTip();
    }

    public void completeDoors() {
        if (doors.isEmpty()) {
            this.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(LangManager.getLang("setup-door-not-set")));
            return;
        }

        // 选区操作
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

        BingDungeon.instance.Rooms.getConfig().getInt(id + ".y-offset", yOffset);
        ArrayList<String> doorsList = new ArrayList<>();
        for (Door door : doors) {
            doorsList.add(door.getX() + "," + door.getZ() + "," + door.getType());
        }
        BingDungeon.instance.Rooms.getConfig().set(id + ".doors", doorsList);

        BingDungeon.instance.Rooms.saveConfig();

        // 保存提示
        TextComponent textComponent = new TextComponent(LangManager.getLang("setup-save-success-msg", id));
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bingdungeon setup"));
        player.spigot().sendMessage(textComponent);

        cancel();
    }
}
