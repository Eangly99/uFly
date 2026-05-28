package dev.naruto.uFly.manager;

import dev.naruto.uFly.UFlyPlugin;
import dev.naruto.uFly.config.ConfigManager;
import dev.naruto.uFly.model.FlySession;
import dev.naruto.uFly.util.MessageUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    /**
     * Result type for fly toggle operations.
     */
    public enum ToggleResult {
        ENABLED, DISABLED,
        DENIED_NO_PERMISSION,
        DENIED_COMBAT,
        DENIED_REGION,
        DENIED_PLOT
    }

    /**
     * Attempts to toggle fly for the player. Applies all guard checks in priority order.
     *
     * @param player   the player requesting the toggle
     * @param autoMode if true, this was triggered automatically (plot enter), not by command
     * @return the result of the toggle attempt
     */
    public @NotNull ToggleResult toggleFly(@NotNull Player player, boolean autoMode) {
        // 1. Base permission
        if (!player.hasPermission("ufly.use")) return ToggleResult.DENIED_NO_PERMISSION;

        // 2. Combat check
        if (hookManager.getCelestCombatHook().isInCombat(player)) return ToggleResult.DENIED_COMBAT;

        // 3. WorldGuard check
        if (!hookManager.getWorldGuardHook().canFly(player)) return ToggleResult.DENIED_REGION;

        // 4. PlotSquared context check
        if (hookManager.getPlotSquaredHook().isEnabled()) {
            if (!hookManager.getPlotSquaredHook().canFlyAtLocation(player)) {
                return ToggleResult.DENIED_PLOT;
            }
        }

        // 5. Toggle
        if (hasFlySession(player)) {
            disableFly(player, false);
            return ToggleResult.DISABLED;
        } else {
            enableFly(player, autoMode);
            return ToggleResult.ENABLED;
        }
    }

    /** Enables fly for a player and creates a session. */
    public void enableFly(@NotNull Player player, boolean auto) {
        player.setAllowFlight(true);
        player.setFlying(true);
        sessions.put(player.getUniqueId(),
                new FlySession(player.getUniqueId(), auto, player.getWorld().getName()));
    }

    /**
     * Disables fly for a player and removes the session.
     *
     * @param player   the player
     * @param notify   if true, send the outside-plot message
     */
    public void disableFly(@NotNull Player player, boolean notify) {
        player.setFlying(false);
        player.setAllowFlight(false);
        sessions.remove(player.getUniqueId());
        if (notify) {
            MessageUtil.send(player, configManager.getMessage("outside-plot"),
                    configManager.getBoolean("settings.actionbar-notifications"));
        }
    }

    /** Returns true if the player currently has an active fly session. */
    public boolean hasFlySession(@NotNull Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    /** Returns the fly session for a player, or null. */
    public @Nullable FlySession getSession(@NotNull Player player) {
        return sessions.get(player.getUniqueId());
    }

    /** Removes a player session without touching flight state (e.g. on quit). */
    public void cleanupSession(@NotNull Player player) {
        sessions.remove(player.getUniqueId());
    }

    /** Disables fly for all tracked players (called on plugin disable). */
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

    /** Re-evaluates fly permission for a player at their current location. */
    public void reEvaluate(@NotNull Player player) {
        if (!hasFlySession(player)) return;

        FlySession session = getSession(player);
        if (session == null) return;

        boolean autoDisable = configManager.getBoolean("settings.auto-disable-on-exit");
        if (!autoDisable) return;

        // If plot check is active and player no longer qualifies, disable
        if (hookManager.getPlotSquaredHook().isEnabled()) {
            if (!hookManager.getPlotSquaredHook().canFlyAtLocation(player)) {
                if (session.autoEnabled()) {
                    disableFly(player, true);
                }
            }
        }
    }
}
