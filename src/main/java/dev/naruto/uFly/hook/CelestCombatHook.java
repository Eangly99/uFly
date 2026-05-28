package dev.naruto.uFly.hook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public class CelestCombatHook implements PluginHook {

    private final JavaPlugin plugin;
    private boolean enabled = false;
    private Plugin celestPlugin;
    private Method isInCombatMethod;

    public CelestCombatHook(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void onEnable() {
        celestPlugin = Bukkit.getPluginManager().getPlugin("CelestCombat-Pro");
        if (celestPlugin == null) {
            // Try alternate name
            celestPlugin = Bukkit.getPluginManager().getPlugin("CelestCombat");
        }
        if (celestPlugin != null) {
            try {
                // Attempt to locate API class via reflection — tolerates API changes
                Class<?> apiClass = Class.forName("me.celest.combat.api.CelestCombatAPI");
                isInCombatMethod = apiClass.getMethod("isInCombat", Player.class);
                enabled = true;
                plugin.getLogger().info("[uFly] CelestCombat-Pro hook enabled.");
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                plugin.getLogger().warning("[uFly] CelestCombat-Pro found but API incompatible: " + e.getMessage());
            }
        }
    }

    @Override
    public void onDisable() {
        enabled = false;
        celestPlugin = null;
        isInCombatMethod = null;
    }

    /**
     * Returns true if the player is currently in combat according to CelestCombat-Pro.
     * Always returns false if the hook is not active.
     */
    public boolean isInCombat(@NotNull Player player) {
        if (!enabled || isInCombatMethod == null) return false;
        try {
            Object result = isInCombatMethod.invoke(null, player);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().warning("[uFly] CelestCombat combat check failed: " + e.getMessage());
            }
            return false;
        }
    }
}
