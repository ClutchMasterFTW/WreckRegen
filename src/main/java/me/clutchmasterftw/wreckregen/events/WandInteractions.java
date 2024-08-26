package me.clutchmasterftw.wreckregen.events;

import me.clutchmasterftw.wreckregen.ToggleVisualization;
import me.clutchmasterftw.wreckregen.WreckRegen;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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

                player.sendMessage(WreckRegen.PREFIX + "You set the block at " + ChatColor.YELLOW + x + ", " + y + ", " + z + ChatColor.WHITE + " to regenerate as " + ChatColor.YELLOW + blockType + ChatColor.WHITE + ".");

                e.setCancelled(true);

                List<Map<?, ?>> blocksToRegen = FILE.getMapList("blocks-to-regen");
                for(int i = 0; i < blocksToRegen.size(); i++) {
                    int blockInfoX = (int) blocksToRegen.get(i).get("x");
                    int blockInfoY = (int) blocksToRegen.get(i).get("y");
                    int blockInfoZ = (int) blocksToRegen.get(i).get("z");
//                String blockInfoType = (String) blocksToRegen.get(i).get("type");

                    if(x == blockInfoX && y == blockInfoY && z == blockInfoZ) {
                        player.sendMessage(WreckRegen.PREFIX + "There is already a block set to regen at this position. Resetting its block type in the config to " + ChatColor.YELLOW + blockType + ".");

                        //Remove the old version of the block at the location in the config
                        blocksToRegen.remove(i);
                        break;
                    }
                }
                //No matter what (even if the condition in the for loop above is true), insert the block Map back into the config.
                Map<String, Object> blockMap = new HashMap<>();
                blockMap.put("x", x);
                blockMap.put("y", y);
                blockMap.put("z", z);
                blockMap.put("type", blockType);

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
