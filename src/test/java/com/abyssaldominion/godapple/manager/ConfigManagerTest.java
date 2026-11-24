package com.abyssaldominion.godapple.manager;

import com.abyssaldominion.godapple.AbyssalDominionGodapple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigManagerTest {
    @TempDir Path directory;
    ConfigManager manager;
    @BeforeEach void setup() {
        var plugin = mock(AbyssalDominionGodapple.class);
        when(plugin.getDataFolder()).thenReturn(directory.toFile());
        when(plugin.getLogger()).thenReturn(Logger.getAnonymousLogger());
        manager = new ConfigManager(plugin);
    }
    void write(String enabled, String amplifier, String duration) throws Exception {
        Files.writeString(directory.resolve("config.yml"), "regeneration:\n  enabled: " + enabled + "\n  amplifier: " + amplifier + "\n  duration-seconds: " + duration + "\n");
    }
    @Test void amplifierZeroMeansLevelOne() throws Exception {
        write("true", "0", "30");
        manager.load();
        assertEquals(0, manager.getAmplifier());
    }
    @Test void invalidAmplifierKeepsPreviousWholeConfiguration() throws Exception {
        write("false", "1", "40"); manager.load();
        write("true", "-1", "90"); manager.reload();
        assertFalse(manager.isEnabled());
        assertEquals(1, manager.getAmplifier());
        assertEquals(800, manager.getDurationTicks());
    }
    @Test void overflowAndWrongTypesCannotReplaceValidConfiguration() throws Exception {
        write("true", "3", "40"); manager.load();
        for (String duration : new String[]{"0", "-5", "214748365", "4294967297", "2.5", "'thirty'"}) {
            write("false", "2", duration); manager.reload();
            assertEquals(800, manager.getDurationTicks(), duration);
            assertTrue(manager.isEnabled(), duration);
            assertEquals(3, manager.getAmplifier(), duration);
        }
    }
    @Test void wrongBooleanAndAmplifierTypesAreRejected() throws Exception {
        write("true", "3", "40"); manager.load();
        for (String amplifier : new String[]{"256", "4294967296", "2.5", "'two'"}) {
            write("false", amplifier, "20"); manager.reload();
            assertEquals(3, manager.getAmplifier(), amplifier);
        }
        write("'false'", "1", "20"); manager.reload();
        assertTrue(manager.isEnabled());
        assertEquals(3, manager.getAmplifier());
    }
    @Test void malformedOrMissingSectionsAreNotRewrittenAndKeepPreviousConfiguration() throws Exception {
        try (var bukkit = mockStatic(org.bukkit.Bukkit.class)) {
        bukkit.when(org.bukkit.Bukkit::getLogger).thenReturn(Logger.getAnonymousLogger());
        write("false", "1", "40"); manager.load();
        for (String content : new String[]{"broken: [", "unrelated: true\n"}) {
            Files.writeString(directory.resolve("config.yml"), content);
            manager.reload();
            assertFalse(manager.isEnabled());
            assertEquals(1, manager.getAmplifier());
            assertEquals(content, Files.readString(directory.resolve("config.yml")));
        }
        }
    }
    @Test void validReloadRefreshesAllRuntimeValues() throws Exception {
        write("false", "1", "40"); manager.load();
        write("true", "4", "10"); manager.reload();
        assertTrue(manager.isEnabled());
        assertEquals(4, manager.getAmplifier());
        assertEquals(200, manager.getDurationTicks());
    }
}
