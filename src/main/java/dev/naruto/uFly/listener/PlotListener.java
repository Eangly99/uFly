package dev.naruto.uFly.listener;

import com.plotsquared.bukkit.player.BukkitPlayer;
import com.plotsquared.core.events.PlotDeleteEvent;
import com.plotsquared.core.events.PlayerEnterPlotEvent;
import com.plotsquared.core.events.PlayerLeavePlotEvent;
import com.plotsquared.core.player.PlotPlayer;
import dev.naruto.uFly.UFlyPlugin;
import dev.naruto.uFly.manager.FlyManager;
import dev.naruto.uFly.model.FlySession;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlotListener implements Listener {

    private final UFlyPlugin plugin;
    private final FlyManager flyManager;

    public PlotListener(@NotNull UFlyPlugin plugin, @NotNull FlyManager flyManager) {
        this.plugin = plugin;
        this.flyManager = flyManager;
    }

    /** Converts a PlotPlayer<?> to a Bukkit Player, or null if not a BukkitPlayer. */
    private @Nullable Player toPlayer(@NotNull PlotPlayer<?> plotPlayer) {
        if (plotPlayer instanceof BukkitPlayer bp) {
            return bp.getPlatformPlayer();
        }
        return null;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnterPlot(@NotNull PlayerEnterPlotEvent event) {
        Player player = toPlayer(event.getPlotPlayer());
        if (player == null) return;
        if (flyManager.hasFlySession(player)) return;
        if (!plugin.getConfigManager().getBoolean("settings.auto-disable-on-exit")) return;
        if (plugin.getHookManager().getPlotSquaredHook().canFlyAtLocation(player)) {
            flyManager.enableFly(player, true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeavePlot(@NotNull PlayerLeavePlotEvent event) {
        Player player = toPlayer(event.getPlotPlayer());
        if (player == null) return;
        FlySession session = flyManager.getSession(player);
        if (session == null) return;
        if (!plugin.getConfigManager().getBoolean("settings.auto-disable-on-exit")) return;
        boolean allowRoad = plugin.getConfigManager().getBoolean("settings.allow-fly-in-road");
        if (session.autoEnabled() && !allowRoad) {
            flyManager.disableFly(player, true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlotDelete(@NotNull PlotDeleteEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            FlySession session = flyManager.getSession(player);
            if (session == null) continue;
            if (!session.worldName().equals(event.getPlot().getWorldName())) continue;
            var currentPlot = plugin.getHookManager().getPlotSquaredHook().getPlotAt(player);
            if (currentPlot != null && currentPlot.getId().equals(event.getPlot().getId())) {
                flyManager.disableFly(player, true);
            }
        }
    }
}
