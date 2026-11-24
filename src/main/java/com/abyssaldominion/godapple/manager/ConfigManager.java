package com.abyssaldominion.godapple.manager;

import com.abyssaldominion.godapple.AbyssalDominionGodapple;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;

/** Validated immutable runtime settings. Reload never writes the operator's file. */
public final class ConfigManager {
    private final AbyssalDominionGodapple plugin;
    private final File configFile;
    private volatile Settings settings = new Settings(true, 2, 600);

    public record Settings(boolean enabled, int amplifier, int durationTicks) {}

    public ConfigManager(AbyssalDominionGodapple plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
    }

    public boolean load() { return reload(); }

    public synchronized boolean reload() {
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(configFile);
            ConfigurationSection regeneration = yaml.getConfigurationSection("regeneration");
            if (regeneration == null) throw new IllegalArgumentException("Missing 'regeneration' section.");
            Object enabled = regeneration.get("enabled", true);
            if (!(enabled instanceof Boolean)) throw new IllegalArgumentException("regeneration.enabled must be a boolean.");
            int amplifier = integer(regeneration, "amplifier", 2, 0, 255);
            int seconds = integer(regeneration, "duration-seconds", 30, 1, Integer.MAX_VALUE / 20);
            settings = new Settings((Boolean) enabled, amplifier, Math.multiplyExact(seconds, 20));
            return true;
        } catch (IOException | InvalidConfigurationException | IllegalArgumentException failure) {
            plugin.getLogger().warning("Configuration rejected; previous valid settings retained: " + failure.getMessage());
            return false;
        }
    }

    private int integer(ConfigurationSection section, String key, int fallback, int min, int max) {
        Object raw = section.get(key, fallback);
        if (!(raw instanceof Integer || raw instanceof Long)) throw new IllegalArgumentException("regeneration." + key + " must be an integer.");
        long number = ((Number) raw).longValue();
        if (number < min || number > max) throw new IllegalArgumentException("regeneration." + key + " must be between " + min + " and " + max + ".");
        return (int) number;
    }

    public Settings snapshot() { return settings; }
    public boolean isEnabled() { return settings.enabled(); }
    public int getAmplifier() { return settings.amplifier(); }
    public int getDurationTicks() { return settings.durationTicks(); }
}
