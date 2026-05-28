package dev.naruto.uFly.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class ConfigManager {

    private final JavaPlugin plugin;
    private static final MiniMessage MINI = MiniMessage.miniMessage();

    public ConfigManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /** Saves default config and loads it from disk. */
    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
    }

    /** Reloads config from disk (for /ufly reload). */
    public void reload() {
        plugin.reloadConfig();
    }

    /**
     * Returns a parsed MiniMessage Component for a message key.
     * Falls back to the raw key string if the path is missing.
     *
     * @param key the message key under messages.* in config.yml
     */
    public @NotNull Component getMessage(@NotNull String key) {
        String prefix = plugin.getConfig().getString("messages.prefix", "");
        String msg = plugin.getConfig().getString("messages." + key, "<red>[uFly] Unknown message: " + key);
        return MINI.deserialize(prefix + msg);
    }

    /**
     * Returns a raw string from config.
     *
     * @param path the full config path
     */
    public @NotNull String getString(@NotNull String path) {
        return plugin.getConfig().getString(path, "");
    }

    /**
     * Returns a boolean setting from config.
     *
     * @param path the full config path
     */
    public boolean getBoolean(@NotNull String path) {
        return plugin.getConfig().getBoolean(path, false);
    }
}
