package dev.naruto.uFly.listener;

import com.plotsquared.bukkit.player.BukkitPlayer;
import com.plotsquared.core.events.PlotDeleteEvent;
import com.plotsquared.core.events.PlayerEnterPlotEvent;
import com.plotsquared.core.events.PlayerExitPlotEvent;
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnterPlot(@NotNull PlayerEnterPlotEvent event) {
        if (!(event.getPlayer() instanceof BukkitPlayer bp)) return;
        Player player = bp.getBukkitPlayer();
        if (player == null) return;
        if (flyManager.hasFlySession(player)) return;
        if (!plugin.getConfigManager().getBoolean("settings.auto-disable-on-exit")) return;
        if (plugin.getHookManager().getPlotSquaredHook().canFlyAtLocation(player)) {
            flyManager.enableFly(player, true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExitPlot(@NotNull PlayerExitPlotEvent event) {
        if (!(event.getPlayer() instanceof BukkitPlayer bp)) return;
        Player player = bp.getBukkitPlayer();
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
            var ps = plugin.getHookManager().getPlotSquaredHook();
            var currentPlot = ps.getPlotAt(player);
            if (currentPlot != null && currentPlot.getId().equals(event.getPlot().getId())) {
                flyManager.disableFly(player, true);
            }
        }
    }
}
