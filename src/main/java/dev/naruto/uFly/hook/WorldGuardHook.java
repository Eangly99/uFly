package dev.naruto.uFly.hook;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class WorldGuardHook implements PluginHook {

    private final JavaPlugin plugin;
    private boolean enabled = false;

    public WorldGuardHook(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            try {
                // Access WorldGuard platform to verify it's properly loaded
                WorldGuard.getInstance();
                enabled = true;
                plugin.getLogger().info("[uFly] WorldGuard hook enabled.");
            } catch (Exception e) {
                plugin.getLogger().warning("[uFly] WorldGuard hook failed: " + e.getMessage());
            }
        }
    }

    @Override
    public void onDisable() {
        enabled = false;
    }

    /**
     * Returns true if WorldGuard allows flight at the player's location.
     * Falls back to true (allow) if WorldGuard is not present or no explicit flag is set.
     */
    public boolean canFly(@NotNull Player player) {
        if (!enabled) return true;
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            com.sk89q.worldedit.util.Location weLoc = BukkitAdapter.adapt(player.getLocation());
            com.sk89q.worldguard.LocalPlayer wgPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
            // Use PVP flag as a proxy; a dedicated FLY flag would require WorldGuard API extension.
            // Here we check the standard FLY flag if available (WG 7+)
            StateFlag.State state = query.queryState(weLoc, wgPlayer, Flags.FLY);
            if (state == StateFlag.State.DENY) return false;
            return true;
        } catch (Exception e) {
            if (plugin.getConfig().getBoolean("settings.debug", false)) {
                plugin.getLogger().warning("[uFly] WorldGuard fly check error: " + e.getMessage());
            }
            return true;
        }
    }
}
