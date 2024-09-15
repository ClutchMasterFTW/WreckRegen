package me.clutchmasterftw.wreckregen;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

public class Commands implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args[0].equals("wand") && sender.hasPermission("wreckregen.admin")) {
            ItemStack wand = Wand.giveWand();

            if (sender instanceof Player) {
                Player player = (Player) sender;
                PlayerInventory inventory = player.getInventory();

                for (int i = 0; i < 36; i++) {
                    if (inventory.getItem(i) == null) {
                        //Empty slot
                        inventory.setItem(i, wand);
                        player.sendMessage(WreckRegen.PREFIX + "You've received a Wand!");

                        return true;
                    }
                }

                player.sendMessage(WreckRegen.PREFIX + ChatColor.RED + "You need an empty slot in your inventory to receive a wand!");
                return true;
            }
        } else if(args[0].equals("regen") && sender.hasPermission("wreckregen.admin")) {
            WreckRegen.regenerateBlocks((Player) sender);
        }
        return true;
    }
}
