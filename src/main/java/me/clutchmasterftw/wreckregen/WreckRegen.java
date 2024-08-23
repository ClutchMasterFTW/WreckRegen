package me.clutchmasterftw.wreckregen;

import me.clutchmasterftw.wreckregen.events.WandInteractions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class WreckRegen extends JavaPlugin {
    public static final String PREFIX = ChatColor.AQUA + "Wreck" + ChatColor.BLUE + "Regen" + ChatColor.GRAY + " » " + ChatColor.RESET;

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

        this.getCommand("wreckregen").setExecutor(new GiveWand());
        Bukkit.getServer().getPluginManager().registerEvents(new WandInteractions(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
