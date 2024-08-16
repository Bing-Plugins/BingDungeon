package cn.yistars.dungeon.setup.tip;

import cn.yistars.dungeon.BingDungeon;
import cn.yistars.dungeon.config.LangManager;
import cn.yistars.dungeon.room.door.SetupDoor;
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

    public void sendRegionTip() {
        ArrayList<TextComponent> lines = new ArrayList<>();
        String text;
        switch (setupPlayer.getSetupType()) {
            case ROOM:
                text = LangManager.getLang("setup-room-region-tip");
                break;
            case ROAD:
                text = LangManager.getLang("setup-road-region-tip");
                break;
            default:
                return;
        }

        for (String line : text.split("(?<=\n)")) {
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
                    lines.addAll(getRegionButton());
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
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/bingdungeon setup-" + setupPlayer.getSetupType().toString().toLowerCase() + " id "));
        return textComponent;
    }

    private TextComponent getRoomTypeStatus(String line) {
        if (setupPlayer.getRoomType() == null) {
            line = line.replace("%type-status%", getLevel(TipLevel.UNSET) + LangManager.getLang("setup-type-status", LangManager.getLang("setup-unset")));
        } else {
            line = line.replace("%type-status%", getLevel(TipLevel.SUCCESS) + LangManager.getLang("setup-type-status", setupPlayer.getRoomType().toString()));
        }

        TextComponent textComponent = new TextComponent(line);
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/bingdungeon setup-" + setupPlayer.getSetupType().toString().toLowerCase() + " type "));
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
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bingdungeon setup-" + setupPlayer.getSetupType().toString().toLowerCase() + " get-stick"));
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
                hovers.add(new Text(LangManager.getLang("setup-" + setupPlayer.getSetupType().toString().toLowerCase() + "-location-hover-not-allow-size", BingDungeon.instance.getConfig().getString("unit-size"))));
            }

            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hovers));
        }

        return textComponent;
    }

    private ArrayList<TextComponent> getRegionButton() {
        ArrayList<TextComponent> lines = new ArrayList<>();
        TextComponent saveButton;
        if (setupPlayer.canSaveRegion()) {
            saveButton = new TextComponent(getLevel(TipLevel.SUCCESS) + LangManager.getLang("setup-save-region-button"));
            saveButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bingdungeon setup-" + setupPlayer.getSetupType().toString().toLowerCase() + " save-region"));
        } else {
            saveButton = new TextComponent(getLevel(TipLevel.UNSET) + LangManager.getLang("setup-save-region-button"));
        }
        lines.add(saveButton);
        // 中间
        lines.add(new TextComponent(LangManager.getLang("setup-button-split")));
        // 取消按钮
        TextComponent cancelButton = new TextComponent(LangManager.getLang("setup-cancel-button"));
        cancelButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bingdungeon setup-" + setupPlayer.getSetupType().toString().toLowerCase() + " cancel"));
        lines.add(cancelButton);

        return lines;
    }

    public void sendDoorsTip() {
        ArrayList<TextComponent> lines = new ArrayList<>();
        for (String line : LangManager.getLang("setup-doors-tip").split("(?<=\n)")) {
            switch(line.replace("\n", "").toLowerCase()) {
                case "%id-info%":
                    lines.add(getIDInfo(line));
                    break;
                case "%doors-status%":
                    lines.addAll(getDoorsStatus());
                    break;
                case "%complete-button%":
                    lines.addAll(getDoorsButton());
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

    private TextComponent getIDInfo(String line) {
        line = line.replace("%id-info%", LangManager.getLang("setup-" + setupPlayer.getSetupType().toString().toLowerCase() + "-door-id-status", setupPlayer.getId()));

        return new TextComponent(line);
    }

    private ArrayList<TextComponent> getDoorsStatus() {
        ArrayList<TextComponent> lines = new ArrayList<>();
        TextComponent status = new TextComponent(LangManager.getLang("setup-" + setupPlayer.getSetupType().toString().toLowerCase() + "-door-status", String.valueOf(setupPlayer.getDoors().size())));
        lines.add(status);

        for (SetupDoor door : setupPlayer.getDoors()) {
            TextComponent doorText = new TextComponent(LangManager.getLang(
                    "setup-" + setupPlayer.getSetupType().toString().toLowerCase() + "-door-location-format",
                    String.valueOf(door.getLocation().getBlockX()),
                    String.valueOf(door.getLocation().getBlockY()),
                    String.valueOf(door.getLocation().getBlockZ()),
                    door.getType().toString(),
                    setupPlayer.getYOffset().toString()
            ));

            doorText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(
                    LangManager.getLang("setup-" + setupPlayer.getSetupType().toString().toLowerCase() + "-door-location-hover",
                            String.valueOf(door.getX()),
                            String.valueOf(door.getZ()),
                            String.valueOf(setupPlayer.getYOffset()),
                            door.getType().toString()
                    )
            )));

            lines.add(doorText);

            lines.add(new TextComponent(LangManager.getLang("setup-door-location-status-split")));
        }

        if (lines.size() >= 3) {
            lines.remove(lines.size() - 1);
        }

        return lines;
    }

    private ArrayList<TextComponent> getDoorsButton() {
        ArrayList<TextComponent> lines = new ArrayList<>();
        TextComponent saveButton;
        if (!setupPlayer.getDoors().isEmpty()) {
            saveButton = new TextComponent(getLevel(TipLevel.SUCCESS) + LangManager.getLang("setup-save-doors-button"));
            saveButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bingdungeon setup-" + setupPlayer.getSetupType().toString().toLowerCase() + " save-doors"));
        } else {
            saveButton = new TextComponent(getLevel(TipLevel.UNSET) + LangManager.getLang("setup-save-doors-button"));
        }
        lines.add(saveButton);
        // 中间
        lines.add(new TextComponent(LangManager.getLang("setup-complete-button-split")));
        // 清除所有门
        TextComponent clearButton;
        if (!setupPlayer.getDoors().isEmpty()) {
            clearButton = new TextComponent(getLevel(TipLevel.WARNING) + LangManager.getLang("setup-clear-doors-button"));
            clearButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bingdungeon setup-" + setupPlayer.getSetupType().toString().toLowerCase() + " clear-doors"));
        } else {
            clearButton = new TextComponent(getLevel(TipLevel.UNSET) + LangManager.getLang("setup-clear-doors-button"));
        }
        lines.add(clearButton);
        // 中间
        lines.add(new TextComponent(LangManager.getLang("setup-complete-button-split")));
        // 取消按钮
        TextComponent cancelButton = new TextComponent(LangManager.getLang("setup-cancel-button"));
        cancelButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bingdungeon setup-" + setupPlayer.getSetupType().toString().toLowerCase() + " cancel"));
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
