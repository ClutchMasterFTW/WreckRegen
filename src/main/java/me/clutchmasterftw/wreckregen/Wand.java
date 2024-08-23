package me.clutchmasterftw.wreckregen;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class Wand extends ItemStack {
    public static ItemStack giveWand() {
        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "WreckRegen Wand");
        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.RESET + "" + ChatColor.YELLOW + "Left click a block to set it to regen.");
        lore.add(ChatColor.RESET + "" + ChatColor.YELLOW + "Right click a block to unset it.");
        lore.add(ChatColor.RESET + "" + ChatColor.YELLOW + "Shift+Right click to see the current regen blocks.");
        lore.add(ChatColor.RESET + "" + ChatColor.RED + "NOTE: Refrain from working with blocks");
        lore.add(ChatColor.RESET + "" + ChatColor.RED + "that have gravity or change locations!");
        meta.setLore(lore);

        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(new NamespacedKey(WreckRegen.getPlugin(), "isWreckRegenWand"), PersistentDataType.BOOLEAN, true);

        wand.setItemMeta(meta);

        return wand;
    }
}
