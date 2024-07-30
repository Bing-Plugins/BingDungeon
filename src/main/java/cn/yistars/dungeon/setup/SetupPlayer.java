package cn.yistars.dungeon.setup;

import cn.yistars.dungeon.BingDungeon;
import cn.yistars.dungeon.config.LangManager;
import cn.yistars.dungeon.room.RoomType;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
        BingDungeon.instance.Rooms.saveConfig();

        // 保存提示
        TextComponent textComponent = new TextComponent(LangManager.getLang("setup-save-success-msg", id));
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bingdungeon setup"));
        player.spigot().sendMessage(textComponent);

        cancel();
    }
}
