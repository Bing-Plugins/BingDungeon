package cn.yistars.dungeon.road;

import cn.yistars.dungeon.BingDungeon;
import cn.yistars.dungeon.room.door.DoorType;

import java.util.HashSet;

public class RoadManager {
    public static HashSet<PreloadRoadFile> preloadRoadFiles = new HashSet<>();

    public static void initRoadFiles() {
        preloadRoadFiles.clear();

        for (String key : BingDungeon.instance.Roads.getConfig().getConfigurationSection("").getKeys(false)) {
            preloadRoadFiles.add(new PreloadRoadFile(key));
        }
    }

    public static HashSet<PreloadRoad> getPreloadRoads(HashSet<DoorType> facings) {
        HashSet<PreloadRoad> preloadRoads = new HashSet<>();

        for (PreloadRoadFile preloadRoadFile : preloadRoadFiles) {
            if (preloadRoadFile.getOriginalFacings().size() != facings.size()) continue;
            for (PreloadRoad preloadRoad : preloadRoadFile.getPreloadRoads()) {
                if (!preloadRoad.getFacings().containsAll(facings)) continue;
                preloadRoads.add(preloadRoad);
            }
        }

        return preloadRoads;
    }
}
