package com.abyssaldominion.godapple.command;

import com.abyssaldominion.godapple.AbyssalDominionGodapple;
import com.abyssaldominion.godapple.manager.ConfigManager;
import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.List;
import java.util.logging.Logger;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GodappleCommandTest {
    AbyssalDominionGodapple plugin;
    ConfigManager config;
    CommandSender sender;
    GodappleCommand command;
    @BeforeEach void setup() {
        plugin = mock(AbyssalDominionGodapple.class);
        config = mock(ConfigManager.class);
        when(plugin.getConfigManager()).thenReturn(config);
        when(plugin.getLogger()).thenReturn(Logger.getAnonymousLogger());
        sender = mock(CommandSender.class);
        PluginMeta meta = mock(PluginMeta.class);
        when(plugin.getPluginMeta()).thenReturn(meta);
        when(meta.getName()).thenReturn("AbyssalDominionGodapple");
        when(meta.getVersion()).thenReturn("9.8.7-test");
        command = new GodappleCommand(plugin);
    }
    private List<Component> messages() {
        var captor = ArgumentCaptor.forClass(Component.class);
        verify(sender, atLeastOnce()).sendMessage(captor.capture());
        return captor.getAllValues();
    }
    private String text() { return messages().stream().map(PlainTextComponentSerializer.plainText()::serialize).reduce("", (a,b) -> a + "\n" + b); }
    @Test void publicVersionIsDynamicStyledAndHasTruthfulProvenance() {
        when(sender.hasPermission("abyssaldominion.godapple.version")).thenReturn(true);
        assertTrue(command.onCommand(sender, null, "abyssaldominiongodapple", new String[]{"version"}));
        assertTrue(text().contains("v9.8.7-test"));
        assertTrue(text().contains("Author: simp0ol"));
        assertTrue(text().contains("Created: Unknown (not recorded)"));
        for (Component message : messages()) {
            assertEquals(Key.key("minecraft:default"), message.font());
            assertEquals(TextDecoration.State.FALSE, message.decoration(TextDecoration.ITALIC));
            assertTrue(PlainTextComponentSerializer.plainText().serialize(message).startsWith("[Abyssal Dominion · Godapple] "));
        }
        verify(config, never()).reload();
    }
    @Test void versionPermissionDenialDoesNotLeakMetadata() {
        command.onCommand(sender, null, "adgodapple", new String[]{"version"});
        assertTrue(text().contains("permission"));
        assertFalse(text().contains("9.8.7"));
    }
    @Test void deniedReloadCannotTouchConfiguration() {
        command.onCommand(sender, null, "adgodapple", new String[]{"reload"});
        verify(config, never()).reload();
        assertTrue(text().contains("permission"));
    }
    @Test void invalidArgumentsDoNotReloadAndHelpIsPermissionFiltered() {
        when(sender.hasPermission("abyssaldominion.godapple.version")).thenReturn(true);
        command.onCommand(sender, null, "adgodapple", new String[]{"reload", "unexpected"});
        verify(config, never()).reload();
        assertTrue(text().contains("/adgodapple version"));
        assertFalse(text().contains("/adgodapple reload"));
    }
    @Test void failedReloadReportsPreviousValidConfigurationRetained() {
        when(sender.hasPermission("abyssaldominion.godapple.reload")).thenReturn(true);
        when(config.reload()).thenReturn(false);
        command.onCommand(sender, null, "adgodapple", new String[]{"reload"});
        assertTrue(text().contains("previous valid configuration"));
        assertFalse(text().contains("reloaded!"));
    }
    @Test void permittedNonOpConsoleCanReload() {
        when(sender.hasPermission("abyssaldominion.godapple.reload")).thenReturn(true);
        when(config.reload()).thenReturn(true);
        command.onCommand(sender, null, "adgodapple", new String[]{"reload"});
        verify(config).reload();
        assertTrue(text().contains("reloaded!"));
    }
    @Test void completionsRespectPermissionsAndPartialInput() {
        when(sender.hasPermission("abyssaldominion.godapple.version")).thenReturn(true);
        TabCompleter tab = assertInstanceOf(TabCompleter.class, command);
        assertEquals(List.of("version"), tab.onTabComplete(sender, null, "adgodapple", new String[]{""}));
        assertEquals(List.of(), tab.onTabComplete(sender, null, "adgodapple", new String[]{"re"}));
    }
}
