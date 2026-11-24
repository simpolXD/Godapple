package com.abyssaldominion.suite;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RpgMessagesTest {
    private String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    @Test void commandMessageUsesCommonPrefixDefaultFontAndNoItalics() {
        Component message = RpgMessages.message("Pets", "Your companion awaits.");
        assertEquals("[Abyssal Dominion · Pets] Your companion awaits.", plain(message));
        assertEquals(Key.key("minecraft:default"), message.font());
        assertEquals(TextDecoration.State.FALSE, message.decoration(TextDecoration.ITALIC));
        assertEquals(TextDecoration.State.FALSE, message.decoration(TextDecoration.OBFUSCATED));
    }

    @Test void playerSuppliedMiniMessageTagsAreLiteralNotMarkup() {
        assertTrue(plain(RpgMessages.message("Trade", "<red>not markup</red>")).contains("<red>not markup</red>"));
    }

    @Test void legacyConfiguredColorsAreStrippedWithoutDamagingAmpersands() {
        assertEquals("[Abyssal Dominion · Coins] Gold & Silver ready", plain(RpgMessages.message("Coins", "&6Gold & Silver §aready")));
        assertEquals("[Abyssal Dominion · Pets] Pet", plain(RpgMessages.message("Pets", "&#A78BFAPet")));
    }

    @Test void successAndErrorUseSamePrefix() {
        CommandSender sender = mock(CommandSender.class);
        RpgMessages.success(sender, "Food Effects", "The tome is renewed.");
        RpgMessages.error(sender, "Food Effects", "Access denied.");
        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(sender, times(2)).sendMessage(captor.capture());
        assertTrue(captor.getAllValues().stream().allMatch(c -> plain(c).startsWith("[Abyssal Dominion · Food Effects] ")));
    }

    @Test void identityUsesPropertiesAndNeverFabricatesMissingCreationDate() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        when(plugin.getResource("suite-info.properties")).thenReturn(new ByteArrayInputStream("created=Unknown (not recorded)\nupdated=2026-11-06\n".getBytes(StandardCharsets.UTF_8)));
        assertEquals("Unknown (not recorded)", RpgMessages.created(plugin));
        assertEquals("2026-11-06", RpgMessages.updated(plugin));
        assertEquals("simp0ol", RpgMessages.AUTHOR);
    }

    @Test void missingIdentityResourceUsesHonestFallback() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        assertEquals("Unknown (not recorded)", RpgMessages.created(plugin));
        assertEquals("Unknown (not recorded)", RpgMessages.updated(plugin));
    }
}
