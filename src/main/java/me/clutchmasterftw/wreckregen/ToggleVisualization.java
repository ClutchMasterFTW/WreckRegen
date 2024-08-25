package me.clutchmasterftw.wreckregen;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class ToggleVisualization {
    public static void toggleVisuals(UUID uuid) {
        final FileConfiguration FILE = WreckRegen.getPlugin().getConfig();
        Player player = Bukkit.getPlayer(uuid);
        // The block used as the visualization for regen blocks:
        final BlockData blockData = Material.RED_STAINED_GLASS.createBlockData();

        if(WreckRegen.playerToggledVisuals.containsKey(uuid)) {
            if(WreckRegen.playerToggledVisuals.get(uuid)) {
                //Player already has visuals
                for(Map<?, ?> block:FILE.getMapList("blocks-to-regen")) {
                    World world = WreckRegen.getPlugin().getServer().getWorld("world");
                    Location location = new Location(world, (int) block.get("x"), (int) block.get("y"), (int) block.get("z"));

                    player.sendBlockChange(location, location.getBlock().getBlockData());
                }

                player.sendMessage(WreckRegen.PREFIX + "Disabled visualization.");
                WreckRegen.playerToggledVisuals.put(uuid, false);
            } else {
                //Player doesn't have visuals
                for(Map<?, ?> block:FILE.getMapList("blocks-to-regen")) {
                    World world = WreckRegen.getPlugin().getServer().getWorld("world");
                    Location location = new Location(world, (int) block.get("x"), (int) block.get("y"), (int) block.get("z"));

                    player.sendBlockChange(location, blockData);
                }

                player.sendMessage(WreckRegen.PREFIX + "Enabled visualization for the current regen blocks set.");
                WreckRegen.playerToggledVisuals.put(uuid, true);
            }
        } else {
            //Player doesn't have visuals
            for(Map<?, ?> block:FILE.getMapList("blocks-to-regen")) {
                World world = WreckRegen.getPlugin().getServer().getWorld("world");
                Location location = new Location(world, (int) block.get("x"), (int) block.get("y"), (int) block.get("z"));

                player.sendBlockChange(location, blockData);
            }

            player.sendMessage(WreckRegen.PREFIX + "Enabled visualization for the current regen blocks set.");
            WreckRegen.playerToggledVisuals.put(uuid, true);
        }
    }
}
