package dev.naruto.uFly.hook;

import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
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

    /** Returns the PlotSquared Plot at the player's current location, or null. */
    public @Nullable Plot getPlotAt(@NotNull Player player) {
        if (!enabled) return null;
        Location loc = BukkitUtil.adapt(player.getLocation());
        return loc.getPlot();
    }

    /** Returns true if the player's world is a PlotSquared plot world. */
    public boolean isPlotWorld(@NotNull Player player) {
        if (!enabled) return false;
        Location loc = BukkitUtil.adapt(player.getLocation());
        return loc.getPlotArea() != null;
    }

    /** Returns the PlotArea for a player's location, or null. */
    public @Nullable PlotArea getPlotArea(@NotNull Player player) {
        if (!enabled) return null;
        Location loc = BukkitUtil.adapt(player.getLocation());
        return loc.getPlotArea();
    }

    /**
     * Checks if a player is allowed to fly based on their plot permissions.
     *
     * @param player the player
     * @return true if the player has fly rights at their current location
     */
    public boolean canFlyAtLocation(@NotNull Player player) {
        if (!enabled) return false;

        // ufly.all → fly anywhere in a plot world
        if (player.hasPermission("ufly.all") && isPlotWorld(player)) return true;

        Plot plot = getPlotAt(player);
        if (plot == null) return false;

        PlotPlayer<?> plotPlayer = BukkitUtil.adapt(player);

        // ufly.claimed → player is owner
        if (player.hasPermission("ufly.claimed") && plot.isOwner(player.getUniqueId())) return true;

        // ufly.trust → player is trusted or added
        if (player.hasPermission("ufly.trust") &&
                (plot.getTrusted().contains(player.getUniqueId()) ||
                        plot.getMembers().contains(player.getUniqueId()))) return true;

        return false;
    }

    public @Nullable PlotAPI getPlotAPI() { return plotAPI; }
}
