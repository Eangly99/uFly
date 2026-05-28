package dev.naruto.uFly.hook;

import com.plotsquared.bukkit.player.BukkitPlayer;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlotSquaredHook implements PluginHook {

    private final JavaPlugin plugin;
    private boolean enabled = false;
    private PlotAPI plotAPI;

    public PlotSquaredHook(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void onEnable() {
        if (org.bukkit.Bukkit.getPluginManager().getPlugin("PlotSquared") != null) {
            try {
                plotAPI = new PlotAPI();
                enabled = true;
                plugin.getLogger().info("[uFly] PlotSquared hook enabled.");
            } catch (Exception e) {
                plugin.getLogger().warning("[uFly] Failed to hook PlotSquared: " + e.getMessage());
            }
        }
    }

    @Override
    public void onDisable() {
        enabled = false;
        plotAPI = null;
    }

    public @Nullable Plot getPlotAt(@NotNull Player player) {
        if (!enabled) return null;
        Location loc = BukkitUtil.adapt(player.getLocation());
        return loc.getPlot();
    }

    public boolean isPlotWorld(@NotNull Player player) {
        if (!enabled) return false;
        Location loc = BukkitUtil.adapt(player.getLocation());
        return loc.getPlotArea() != null;
    }

    public @Nullable PlotArea getPlotArea(@NotNull Player player) {
        if (!enabled) return null;
        Location loc = BukkitUtil.adapt(player.getLocation());
        return loc.getPlotArea();
    }

    public boolean canFlyAtLocation(@NotNull Player player) {
        if (!enabled) return false;
        if (player.hasPermission("ufly.all") && isPlotWorld(player)) return true;
        Plot plot = getPlotAt(player);
        if (plot == null) return false;
        if (player.hasPermission("ufly.claimed") && plot.isOwner(player.getUniqueId())) return true;
        if (player.hasPermission("ufly.trust") &&
                (plot.getTrusted().contains(player.getUniqueId()) ||
                        plot.getMembers().contains(player.getUniqueId()))) return true;
        return false;
    }

    public @Nullable PlotAPI getPlotAPI() { return plotAPI; }
}
