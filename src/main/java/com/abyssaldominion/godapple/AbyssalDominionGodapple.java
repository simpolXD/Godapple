package com.abyssaldominion.godapple;

import com.abyssaldominion.godapple.command.GodappleCommand;
import com.abyssaldominion.godapple.listener.GodappleListener;
import com.abyssaldominion.godapple.manager.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class AbyssalDominionGodapple extends JavaPlugin {

    private static AbyssalDominionGodapple instance;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        configManager = new ConfigManager(this);
        configManager.load();

        getServer().getPluginManager().registerEvents(new GodappleListener(this), this);
        getCommand("adgodapple").setExecutor(new GodappleCommand(this));

        getLogger().info("AbyssalDominionGodapple v" + getPluginMeta().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("AbyssalDominionGodapple v" + getPluginMeta().getVersion() + " disabled!");
    }

    public static AbyssalDominionGodapple getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
