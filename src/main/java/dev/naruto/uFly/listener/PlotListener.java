package dev.naruto.uFly.listener;

import com.plotsquared.bukkit.events.PlayerEnterPlotEvent;
import com.plotsquared.bukkit.events.PlayerExitPlotEvent;
import com.plotsquared.bukkit.player.BukkitPlayer;
import com.plotsquared.core.events.PlotDeleteEvent;
import dev.naruto.uFly.UFlyPlugin;
import dev.naruto.uFly.manager.FlyManager;
import dev.naruto.uFly.model.FlySession;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class PlotListener implements Listener {

    private final UFlyPlugin plugin;
    private final FlyManager flyManager;

    public PlotListener(@NotNull UFlyPlugin plugin, @NotNull FlyManager flyManager) {
        this.plugin = plugin;
        this.flyManager = flyManager;
    }

    /**
     * When a player enters a plot, silently enable fly if they are eligible and don't already have it.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnterPlot(@NotNull PlayerEnterPlotEvent event) {
        if (!(event.getPlotPlayer() instanceof BukkitPlayer bp)) return;
        Player player = bp.getPlayer();
        if (player == null) return;

        if (flyManager.hasFlySession(player)) return; // already flying

        boolean autoDisable = plugin.getConfigManager().getBoolean("settings.auto-disable-on-exit");
        if (!autoDisable) return;

        if (plugin.getHookManager().getPlotSquaredHook().canFlyAtLocation(player)) {
            flyManager.enableFly(player, true);
        }
    }

    /**
     * When a player exits a plot, disable fly if it was auto-enabled by the plugin.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExitPlot(@NotNull PlayerExitPlotEvent event) {
        if (!(event.getPlotPlayer() instanceof BukkitPlayer bp)) return;
        Player player = bp.getPlayer();
        if (player == null) return;

        FlySession session = flyManager.getSession(player);
        if (session == null) return;

        boolean autoDisable = plugin.getConfigManager().getBoolean("settings.auto-disable-on-exit");
        if (!autoDisable) return;

        if (session.autoEnabled()) {
            // Check if they entered another eligible plot immediately (e.g. road between plots)
            boolean allowRoad = plugin.getConfigManager().getBoolean("settings.allow-fly-in-road");
            if (!allowRoad) {
                flyManager.disableFly(player, true);
            }
        }
    }

    /**
     * When a plot is deleted, remove fly from all players currently on it.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlotDelete(@NotNull PlotDeleteEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            FlySession session = flyManager.getSession(player);
            if (session == null) continue;
            if (!session.worldName().equals(event.getPlot().getWorldName())) continue;
            // Attempt to check if they're on the deleted plot
            var ps = plugin.getHookManager().getPlotSquaredHook();
            var currentPlot = ps.getPlotAt(player);
            if (currentPlot != null && currentPlot.getId().equals(event.getPlot().getId())) {
                flyManager.disableFly(player, true);
            }
        }
    }
}
