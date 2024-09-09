package me.clutchmasterftw.wreckregen.events;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.clutchmasterftw.wreckregen.ToggleVisualization;
import me.clutchmasterftw.wreckregen.WreckRegen;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Directional;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WandInteractions implements Listener {
    private final FileConfiguration FILE = WreckRegen.getPlugin().getConfig();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        ItemStack usedItem = e.getPlayer().getInventory().getItemInMainHand();
        ItemMeta meta = usedItem.getItemMeta();
        if(meta == null) {
            return;
        }
        PersistentDataContainer data = meta.getPersistentDataContainer();

        Player player = e.getPlayer();

        if(data.has(new NamespacedKey(WreckRegen.getPlugin(), "isWreckRegenWand"), PersistentDataType.BOOLEAN)) {
            if(player.hasPermission("wreckregen.admin")) {
                Block block = e.getBlock();
                Location location = block.getLocation();
                int x = location.getBlockX();
                int y = location.getBlockY();
                int z = location.getBlockZ();
                String blockType = block.getType().name();
                BlockData blockData = block.getBlockData();
                String blockDirection = "NONE";
                if(blockData instanceof Orientable) {
                    Orientable orientable = (Orientable) blockData;
                    blockDirection = orientable.getAxis().toString();
                }
                String regionName = x + "_" + y + "_" + z + "_" + "regenblock";

                player.sendMessage(WreckRegen.PREFIX + "You set the block at " + ChatColor.YELLOW + x + ", " + y + ", " + z + ChatColor.WHITE + " to regenerate as " + ChatColor.YELLOW + blockType + ChatColor.WHITE + ".");

                e.setCancelled(true);

                List<Map<?, ?>> blocksToRegen = FILE.getMapList("blocks-to-regen");
                Boolean regionExistsAlready = false;
                for(int i = 0; i < blocksToRegen.size(); i++) {
                    int blockInfoX = (int) blocksToRegen.get(i).get("x");
                    int blockInfoY = (int) blocksToRegen.get(i).get("y");
                    int blockInfoZ = (int) blocksToRegen.get(i).get("z");

                    if(x == blockInfoX && y == blockInfoY && z == blockInfoZ) {
                        player.sendMessage(WreckRegen.PREFIX + "There is already a block set to regen at this position. Resetting its block type in the config to " + ChatColor.YELLOW + blockType + ".");

                        regionExistsAlready = true;

                        //Remove the old version of the block at the location in the config
                        blocksToRegen.remove(i);
                        break;
                    }
                }

                if(!regionExistsAlready) {
                    //Setup a WorldGuard region for the block at the coordinates
                    BlockVector3 point = BlockVector3.at(x, y, z);
                    ProtectedCuboidRegion region = new ProtectedCuboidRegion(regionName, point, point);

                    World world = location.getWorld();
                    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                    RegionManager regions = container.get(BukkitAdapter.adapt(world));

                    if(regions != null) {
                        regions.addRegion(region);
                        region.setFlag(Flags.BLOCK_BREAK, StateFlag.State.ALLOW);

                        try {
                            regions.save();
                        } catch(StorageException exception) {
                            exception.printStackTrace();
                        }
                    }
                }

                //No matter what (even if the condition in the for loop above is true), insert the block Map back into the config.
                Map<String, Object> blockMap = new HashMap<>();
                blockMap.put("x", x);
                blockMap.put("y", y);
                blockMap.put("z", z);
                blockMap.put("type", blockType);
                blockMap.put("direction", blockDirection);
                blockMap.put("region", regionName);

                blocksToRegen.add(blockMap);
                FILE.set("blocks-to-regen", blocksToRegen);
                WreckRegen.getPlugin().saveConfig();
            } else {
                player.sendMessage(WreckRegen.PREFIX + ChatColor.RED + "You don't have permission to use this tool! Please contact an administrator for help if you believe this is incorrect.");
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Action action = e.getAction();
        if(action.isLeftClick()) {
            return;
        }

        Player player = e.getPlayer();
        ItemStack item = e.getItem();
        if (item == null) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        Block block = e.getClickedBlock();
        if (meta == null) {
            return;
        }

        PersistentDataContainer data = meta.getPersistentDataContainer();
        if (data.has(new NamespacedKey(WreckRegen.getPlugin(), "isWreckRegenWand"), PersistentDataType.BOOLEAN)) {
            if(player.hasPermission("wreckregen.admin")) {
                if(player.isSneaking()) {
                    // Toggle client-side visualizations
                    ToggleVisualization.toggleVisuals(player.getUniqueId());
                } else {
                    if(block == null) {
                        return;
                    }
                    List<Map<?, ?>> blocksToRegen = FILE.getMapList("blocks-to-regen");
                    Location location = block.getLocation();
                    int x = location.getBlockX();
                    int y = location.getBlockY();
                    int z = location.getBlockZ();

                    int i = 0;
                    for (Map<?, ?> blockToRegen : blocksToRegen) {
                        if ((int) blockToRegen.get("x") == x && (int) blockToRegen.get("y") == y && (int) blockToRegen.get("z") == z) {
                            // WorldGuard region remove
                            World world = location.getWorld();
                            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                            RegionManager regions = container.get(BukkitAdapter.adapt(world));
                            if(regions != null) {
                                regions.removeRegion((String) blockToRegen.get("region"));

                                try {
                                    regions.save();
                                } catch(StorageException exception) {
                                    exception.printStackTrace();
                                }
                            }

                            blocksToRegen.remove(i);
                            FILE.set("blocks-to-regen", blocksToRegen);
                            WreckRegen.getPlugin().saveConfig();

                            player.sendMessage(WreckRegen.PREFIX + "Stopped the block at " + ChatColor.YELLOW + x + ", " + y + ", " + z + ChatColor.WHITE + " from regenerating.");

                            return;
                        }
                        i++;
                    }
                    player.sendMessage(WreckRegen.PREFIX + ChatColor.RED + "This block wasn't set to regenerate!");
                }
            } else {
                player.sendMessage(WreckRegen.PREFIX + ChatColor.RED + "You don't have permission to use this tool! Please contact an administrator for help if you believe this is incorrect.");
            }
        }
    }
}
