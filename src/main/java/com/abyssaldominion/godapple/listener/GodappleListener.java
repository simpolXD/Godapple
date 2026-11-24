package com.abyssaldominion.godapple.listener;

import com.abyssaldominion.godapple.AbyssalDominionGodapple;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GodappleListener implements Listener {

    private final AbyssalDominionGodapple plugin;

    public GodappleListener(AbyssalDominionGodapple plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (!event.getItem().getType().equals(Material.ENCHANTED_GOLDEN_APPLE)) {
            return;
        }

        if (!plugin.getConfigManager().isEnabled()) {
            return;
        }

        Player player = event.getPlayer();

        // Wait 1 tick for vanilla effects to apply, then modify Regeneration
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            // Remove vanilla Regeneration II
            PotionEffect existingRegen = player.getPotionEffect(PotionEffectType.REGENERATION);
            if (existingRegen != null) {
                player.removePotionEffect(PotionEffectType.REGENERATION);
            }

            // Apply Regeneration III with configured duration
            int duration = plugin.getConfigManager().getDurationTicks();
            int amplifier = plugin.getConfigManager().getAmplifier();

            player.addPotionEffect(new PotionEffect(
                PotionEffectType.REGENERATION,
                duration,
                amplifier,
                true,  // ambient
                true,  // particles
                true   // show icon
            ));

            plugin.getLogger().info("Applied Regeneration III to " + player.getName() + " from Enchanted Golden Apple");
        }, 1L);
    }
}
