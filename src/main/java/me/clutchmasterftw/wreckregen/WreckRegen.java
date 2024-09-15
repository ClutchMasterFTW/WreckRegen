package me.clutchmasterftw.wreckregen;

import me.clutchmasterftw.wreckregen.events.WandInteractions;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.material.Directional;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public final class WreckRegen extends JavaPlugin {
    public static final String PREFIX = ChatColor.AQUA + "Wreck" + ChatColor.BLUE + "Regen" + ChatColor.GRAY + " » " + ChatColor.RESET;

    // In memory storage for the toggle states for player visuals, non-persistent
    public static HashMap<UUID, Boolean> playerToggledVisuals = new HashMap<UUID, Boolean>();

    public static WreckRegen getPlugin() {
        return plugin;
    }
    private static WreckRegen plugin;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;

        saveDefaultConfig();

        Logger logger = this.getLogger();
        logger.info("WreckRegen has been enabled!");

        this.getCommand("wreckregen").setExecutor(new Commands());
        Bukkit.getServer().getPluginManager().registerEvents(new WandInteractions(), this);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                regenerateBlocks(null);
            }
        }, 20, this.getConfig().getInt("interval"));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static void regenerateBlocks(@Nullable Player player) {
        //Since the method is static, we can't use "this" here
        WreckRegen.getPlugin().getLogger().info("Regenerated all blocks.");

        final FileConfiguration FILE = WreckRegen.getPlugin().getConfig();
        final List<Map<?, ?>> BLOCKSTOREGEN = FILE.getMapList("blocks-to-regen");

        for(Map<?, ?> block:BLOCKSTOREGEN) {
            World world = WreckRegen.getPlugin().getServer().getWorld("world");

            int x = (int) block.get("x");
            int y = (int) block.get("y");
            int z = (int) block.get("z");
            Location location = new Location(world, x, y, z);
            Material type = Material.valueOf((String) block.get("type"));

            location.getBlock().setType(type);

            if(!block.get("direction").equals("NONE")) {
                BlockData blockData = location.getBlock().getBlockData();
                try {
                    Orientable orientable = (Orientable) blockData;

                    orientable.setAxis(Axis.valueOf((String) block.get("direction")));

                    location.getBlock().setBlockData(orientable);
                } catch (ClassCastException e) {
                    e.printStackTrace();
                    WreckRegen.getPlugin().getLogger().severe("Error regarding DIRECTION occurred at regen block: " + block.get("region"));
                }
            }
        }

        if(player == null) {
            if(FILE.getBoolean("announce")) {
                Bukkit.getServer().broadcastMessage(PREFIX + FILE.getString("announce-message"));
            }
        } else {
            player.sendMessage(PREFIX + "You've manually regenerated all defined blocks.");
        }
    }
}

// Notes about this plugin: Only the primary world has been tested. Other worlds/dimensions haven't been tested to work with this!