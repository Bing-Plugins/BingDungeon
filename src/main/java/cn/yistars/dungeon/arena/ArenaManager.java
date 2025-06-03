package cn.yistars.dungeon.arena;

import cn.yistars.dungeon.BingDungeon;
import cn.yistars.dungeon.init.DebugStorage;
import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.exceptions.CorruptedWorldException;
import com.infernalsuite.asp.api.exceptions.NewerFormatException;
import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.properties.SlimeProperties;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.infernalsuite.asp.loaders.mysql.MysqlLoader;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.UUID;

public class ArenaManager {
    public static HashSet<Arena> arenas = new HashSet<>();
    public static AdvancedSlimePaperAPI asp;
    public static SlimeWorld slimeWorld;
    private static SlimeLoader loader;
    private static final SlimePropertyMap properties = new SlimePropertyMap();

    public static Arena getArena(Player player) {
        for (Arena arena : arenas) {
            if (arena.getPlayers().contains(player)) {
                return arena;
            }
        }
        return null;
    }

    public static void initArena() {
        asp = AdvancedSlimePaperAPI.instance();

        properties.setValue(SlimeProperties.DIFFICULTY, BingDungeon.instance.getConfig().getString("slime-world.difficulty", "normal"));
        properties.setValue(SlimeProperties.SPAWN_X, BingDungeon.instance.getConfig().getInt("slime-world.spawn-x", 0));
        properties.setValue(SlimeProperties.SPAWN_Y, BingDungeon.instance.getConfig().getInt("slime-world.spawn-y", 90));
        properties.setValue(SlimeProperties.SPAWN_Z, BingDungeon.instance.getConfig().getInt("slime-world.spawn-z", 0));
        properties.setValue(SlimeProperties.SPAWN_YAW, (float) BingDungeon.instance.getConfig().getDouble("slime-world.spawn-yaw", 0));
        properties.setValue(SlimeProperties.ALLOW_ANIMALS, BingDungeon.instance.getConfig().getBoolean("slime-world.allow-animals", false));
        properties.setValue(SlimeProperties.ALLOW_MONSTERS, BingDungeon.instance.getConfig().getBoolean("slime-world.allow-monsters", false));
        properties.setValue(SlimeProperties.DRAGON_BATTLE, BingDungeon.instance.getConfig().getBoolean("slime-world.dragon-battle", false));
        properties.setValue(SlimeProperties.PVP, BingDungeon.instance.getConfig().getBoolean("slime-world.pvp", false));
        properties.setValue(SlimeProperties.ENVIRONMENT, BingDungeon.instance.getConfig().getString("slime-world.environment", "normal"));
        properties.setValue(SlimeProperties.WORLD_TYPE, BingDungeon.instance.getConfig().getString("slime-world.world-type", "DEFAULT"));
        properties.setValue(SlimeProperties.DEFAULT_BIOME, BingDungeon.instance.getConfig().getString("slime-world.default-biome", "minecraft:plains"));

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    loader = new MysqlLoader(
                            BingDungeon.instance.getConfig().getString("slime-database.sql-url", "jdbc:mysql://{host}:{port}/{database}?autoReconnect=true&allowMultiQueries=true&useSSL={usessl}"),
                            BingDungeon.instance.getConfig().getString("slime-database.host", "localhost"),
                            BingDungeon.instance.getConfig().getInt("slime-database.port"),
                            BingDungeon.instance.getConfig().getString("slime-database.database", "dungeon"),
                            BingDungeon.instance.getConfig().getBoolean("slime-database.use-ssl"),
                            BingDungeon.instance.getConfig().getString("slime-database.username"),
                            BingDungeon.instance.getConfig().getString("slime-database.password")
                    );

                    slimeWorld = asp.readWorld(loader, BingDungeon.instance.getConfig().getString("slime-world.id", "Dungeon"), true, properties);
                } catch (SQLException | CorruptedWorldException | NewerFormatException | UnknownWorldException |
                         IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.runTaskAsynchronously(BingDungeon.instance);
    }

    public static Arena createArena() {
        return createArena(new DebugStorage());
    }

    public static Arena createArena(DebugStorage debugStorage) {
        Arena arena = new Arena(debugStorage);
        arenas.add(arena);
        return arena;
    }
}
