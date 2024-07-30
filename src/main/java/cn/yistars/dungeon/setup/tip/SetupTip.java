package cn.yistars.dungeon.setup.tip;

import cn.yistars.dungeon.BingDungeon;
import cn.yistars.dungeon.config.LangManager;
import cn.yistars.dungeon.setup.SetupPlayer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class SetupTip {
    private final SetupPlayer setupPlayer;

    public SetupTip(SetupPlayer setupPlayer) {
        this.setupPlayer = setupPlayer;
    }

    public void sendTip() {
        ArrayList<TextComponent> lines = new ArrayList<>();
        for (String line : LangManager.getLang("setup-tip").split("(?<=\n)")) {
            switch(line.replace("\n", "").toLowerCase()) {
                case "%id-status%":
                    lines.add(getIDStatus(line));
                    break;
                case "%type-status%":
                    lines.add(getRoomTypeStatus(line));
                    break;
                case "%first-location-status%":
                    lines.add(getLocationStatus(line, "first", setupPlayer.getFirstLocation(), setupPlayer.isSameWorld(), setupPlayer.isAllowSize()));
                    break;
                case "%second-location-status%":
                    lines.add(getLocationStatus(line, "second", setupPlayer.getSecondLocation(), setupPlayer.isSameWorld(), setupPlayer.isAllowSize()));
                    break;
                case "%complete-button%":
                    lines.addAll(getButton());
                    break;
                default:
                    lines.add(new TextComponent(line));
            }
        }

        TextComponent[] components = new TextComponent[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            components[i] = lines.get(i);
        }

        setupPlayer.getPlayer().spigot().sendMessage(components);
    }

    private TextComponent getIDStatus(String line) {
        if (setupPlayer.getId() == null) {
            line = line.replace("%id-status%", getLevel(TipLevel.UNSET) + LangManager.getLang("setup-id-status", LangManager.getLang("setup-unset")));
        } else {
            line = line.replace("%id-status%", getLevel(TipLevel.SUCCESS) + LangManager.getLang("setup-id-status", setupPlayer.getId()));
        }

        TextComponent textComponent = new TextComponent(line);
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/bingdungeon setup id "));
        return textComponent;
    }

    private TextComponent getRoomTypeStatus(String line) {
        if (setupPlayer.getRoomType() == null) {
            line = line.replace("%type-status%", getLevel(TipLevel.UNSET) + LangManager.getLang("setup-type-status", LangManager.getLang("setup-unset")));
        } else {
            line = line.replace("%type-status%", getLevel(TipLevel.SUCCESS) + LangManager.getLang("setup-type-status", setupPlayer.getRoomType().toString()));
        }

        TextComponent textComponent = new TextComponent(line);
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/bingdungeon setup type "));
        return textComponent;
    }

    private TextComponent getLocationStatus(String line, String num, Location location, Boolean isSameWorld, Boolean isAllowSize) {
        String level;
        if (location == null) {
            level = getLevel(TipLevel.UNSET);
        } else if (!isSameWorld) {
            level = getLevel(TipLevel.WARNING);
        } else if (!isAllowSize) {
            level = getLevel(TipLevel.WARNING);
        } else {
            level = getLevel(TipLevel.SUCCESS);
        }

        if (location == null) {
            line = line.replace("%" + num + "-location-status%", getLevel(TipLevel.UNSET) + LangManager.getLang("setup-" + num + "-location-status", LangManager.getLang("setup-unset")));
        } else {
            line = line.replace("%" + num + "-location-status%", level + LangManager.getLang("setup-" + num + "-location-status", getLocation(location)));
        }

        TextComponent textComponent = new TextComponent(line);
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bingdungeon setup get-stick"));
        if (setupPlayer.getFirstLocation() != null && setupPlayer.getSecondLocation() != null) {
            List<Content> hovers = new ArrayList<>();
            String length = (setupPlayer.isLengthAllowed() ? getLevel(TipLevel.SUCCESS) : getLevel(TipLevel.WARNING)) + setupPlayer.getRegion().getLength();
            String width = (setupPlayer.isWidthAllowed() ? getLevel(TipLevel.SUCCESS) : getLevel(TipLevel.WARNING)) + setupPlayer.getRegion().getWidth();
            String height = getLevel(TipLevel.SUCCESS) + setupPlayer.getRegion().getHeight();

            hovers.add(new Text(LangManager.getLang("setup-location-hover-size", length, width, height)));

            if (!isSameWorld) {
                hovers.add(new Text("\n"));
                hovers.add(new Text(LangManager.getLang("setup-location-hover-not-same-world")));
            }

            if (!isAllowSize) {
                hovers.add(new Text("\n"));
                hovers.add(new Text(LangManager.getLang("setup-location-hover-not-allow-size", BingDungeon.instance.getConfig().getString("unit-size"))));
            }

            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hovers));
        }

        return textComponent;
    }

    private ArrayList<TextComponent> getButton() {
        ArrayList<TextComponent> lines = new ArrayList<>();
        TextComponent saveButton;
        if (setupPlayer.canComplete()) {
            saveButton = new TextComponent(getLevel(TipLevel.SUCCESS) + LangManager.getLang("setup-save-button"));
            saveButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bingdungeon setup complete"));
        } else {
            saveButton = new TextComponent(getLevel(TipLevel.UNSET) + LangManager.getLang("setup-save-button"));
        }
        lines.add(saveButton);
        // 中间
        lines.add(new TextComponent(LangManager.getLang("setup-complete-button-split")));
        // 取消按钮
        TextComponent cancelButton = new TextComponent(LangManager.getLang("setup-cancel-button"));
        cancelButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bingdungeon setup cancel"));
        lines.add(cancelButton);

        return lines;
    }

    private String getLocation(Location location) {
        return LangManager.getLang("setup-location-format", location.getWorld().getName(), String.valueOf(location.getBlockX()), String.valueOf(location.getBlockY()), String.valueOf(location.getBlockZ()));
    }

    private String getLevel(TipLevel level) {
        switch (level) {
            case SUCCESS:
                return LangManager.getLang("setup-status-success");
            case UNSET:
                return LangManager.getLang("setup-status-unset");
            case WARNING:
                return LangManager.getLang("setup-status-warn");
            default:
                return null;
        }
    }
}
