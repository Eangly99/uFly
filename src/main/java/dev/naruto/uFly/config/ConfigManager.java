package dev.naruto.uFly.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ConfigManager {

    private final JavaPlugin plugin;
    private static final MiniMessage MINI = MiniMessage.miniMessage();

    public ConfigManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
    }

    public void reload() {
        plugin.reloadConfig();
    }

    public @NotNull Component getMessage(@NotNull String key) {
        String prefix = plugin.getConfig().getString("messages.prefix", "");
        String msg = plugin.getConfig().getString("messages." + key, "<red>[uFly] Unknown message: " + key);
        return MINI.deserialize(prefix + msg);
    }

    public @NotNull String getString(@NotNull String path) {
        return plugin.getConfig().getString(path, "");
    }

    public boolean getBoolean(@NotNull String path) {
        return plugin.getConfig().getBoolean(path, false);
    }

    public @NotNull List<String> getStringList(@NotNull String path) {
        return plugin.getConfig().getStringList(path);
    }
}
