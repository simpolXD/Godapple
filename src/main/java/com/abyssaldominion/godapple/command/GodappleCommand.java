package com.abyssaldominion.godapple.command;

import com.abyssaldominion.godapple.AbyssalDominionGodapple;
import com.abyssaldominion.suite.RpgMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class GodappleCommand implements CommandExecutor, TabCompleter {
    private static final String MODULE = "Godapple";
    private static final String RELOAD = "abyssaldominion.godapple.reload";
    private static final String VERSION = "abyssaldominion.godapple.version";
    private final AbyssalDominionGodapple plugin;

    public GodappleCommand(AbyssalDominionGodapple plugin) { this.plugin = plugin; }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) { usage(sender, label); return true; }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "version" -> {
                if (allowed(sender, VERSION)) RpgMessages.version(sender, plugin, MODULE);
            }
            case "reload" -> {
                if (!allowed(sender, RELOAD)) return true;
                plugin.getConfigManager().reload();
                RpgMessages.success(sender, MODULE, "AbyssalDominionGodapple configuration reloaded!");
            }
            default -> usage(sender, label);
        }
        return true;
    }

    private boolean allowed(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) return true;
        RpgMessages.error(sender, MODULE, "You do not have permission to use this command.");
        return false;
    }

    private void usage(CommandSender sender, String label) {
        RpgMessages.heading(sender, MODULE, "AbyssalDominionGodapple Usage:");
        if (sender.hasPermission(VERSION)) RpgMessages.send(sender, MODULE, "/" + label + " version - Show plugin version");
        if (sender.hasPermission(RELOAD)) RpgMessages.send(sender, MODULE, "/" + label + " reload - Reload configuration");
    }

    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String partial = args[0].toLowerCase(Locale.ROOT);
            if ("version".startsWith(partial) && sender.hasPermission(VERSION)) completions.add("version");
            if ("reload".startsWith(partial) && sender.hasPermission(RELOAD)) completions.add("reload");
        }
        return completions;
    }
}
