package dev.naruto.uFly.manager;

import dev.naruto.uFly.UFlyPlugin;
import dev.naruto.uFly.config.ConfigManager;
import dev.naruto.uFly.model.FlySession;
import dev.naruto.uFly.util.MessageUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FlyManager {

    private final UFlyPlugin plugin;
    private final ConfigManager configManager;
    private final HookManager hookManager;
    private final ConcurrentHashMap<UUID, FlySession> sessions = new ConcurrentHashMap<>();

    public FlyManager(@NotNull UFlyPlugin plugin,
                      @NotNull ConfigManager configManager,
                      @NotNull HookManager hookManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.hookManager = hookManager;
    }

    public enum ToggleResult {
        ENABLED, DISABLED,
        DENIED_NO_PERMISSION,
        DENIED_COMBAT,
        DENIED_REGION,
        DENIED_PLOT,
        DENIED_WORLD
    }

    /** Checks if the world is in the enabled list. Empty list = all worlds allowed. */
    public boolean isWorldEnabled(@NotNull String worldName) {
        List<String> enabled = configManager.getStringList("settings.enabled-worlds");
        return enabled.isEmpty() || enabled.contains(worldName);
    }

    public @NotNull ToggleResult toggleFly(@NotNull Player player, boolean autoMode) {
        // 1. Base permission
        if (!player.hasPermission("ufly.use")) return DENIED_NO_PERMISSION;

        // 2. World check
        if (!isWorldEnabled(player.getWorld().getName())) return DENIED_WORLD;

        // 3. Combat check
        if (hookManager.getCelestCombatHook().isInCombat(player)) return DENIED_COMBAT;

        // 4. WorldGuard check
        if (!hookManager.getWorldGuardHook().canFly(player)) return DENIED_REGION;

        // 5. PlotSquared context
        if (hookManager.getPlotSquaredHook().isEnabled()) {
            if (!hookManager.getPlotSquaredHook().canFlyAtLocation(player)) return DENIED_PLOT;
        }

        // 6. Toggle
        if (hasFlySession(player)) {
            disableFly(player, false);
            return DISABLED;
        } else {
            enableFly(player, autoMode);
            return ENABLED;
        }
    }

    public void enableFly(@NotNull Player player, boolean auto) {
        player.setAllowFlight(true);
        player.setFlying(true);
        sessions.put(player.getUniqueId(),
                new FlySession(player.getUniqueId(), auto, player.getWorld().getName()));
    }

    public void disableFly(@NotNull Player player, boolean notify) {
        player.setFlying(false);
        player.setAllowFlight(false);
        sessions.remove(player.getUniqueId());
        if (notify) {
            MessageUtil.send(player, configManager.getMessage("outside-plot"),
                    configManager.getBoolean("settings.actionbar-notifications"));
        }
    }

    public boolean hasFlySession(@NotNull Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    public @Nullable FlySession getSession(@NotNull Player player) {
        return sessions.get(player.getUniqueId());
    }

    public void cleanupSession(@NotNull Player player) {
        sessions.remove(player.getUniqueId());
    }

    public void disableAll() {
        for (UUID uuid : sessions.keySet()) {
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.setFlying(false);
                p.setAllowFlight(false);
            }
        }
        sessions.clear();
    }

    public void reEvaluate(@NotNull Player player) {
        if (!hasFlySession(player)) return;
        FlySession session = getSession(player);
        if (session == null) return;
        if (!configManager.getBoolean("settings.auto-disable-on-exit")) return;

        // If world is no longer enabled, disable fly immediately
        if (!isWorldEnabled(player.getWorld().getName())) {
            disableFly(player, true);
            return;
        }

        // Plot check if enabled
        if (hookManager.getPlotSquaredHook().isEnabled()) {
            if (!hookManager.getPlotSquaredHook().canFlyAtLocation(player)) {
                if (session.autoEnabled()) disableFly(player, true);
            }
        }
    }
}
