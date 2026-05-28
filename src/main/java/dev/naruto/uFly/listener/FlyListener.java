package dev.naruto.uFly.listener;

import dev.naruto.uFly.UFlyPlugin;
import dev.naruto.uFly.manager.FlyManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class FlyListener implements Listener {

    private final UFlyPlugin plugin;
    private final FlyManager flyManager;

    public FlyListener(@NotNull UFlyPlugin plugin, @NotNull FlyManager flyManager) {
        this.plugin = plugin;
        this.flyManager = flyManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(@NotNull PlayerMoveEvent event) {
        if (event.getFrom().getChunk().equals(event.getTo().getChunk())) return;
        flyManager.reEvaluate(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(@NotNull PlayerTeleportEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (event.getPlayer().isOnline()) flyManager.reEvaluate(event.getPlayer());
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(@NotNull PlayerQuitEvent event) {
        if (flyManager.hasFlySession(event.getPlayer())) flyManager.disableFly(event.getPlayer(), false);
        flyManager.cleanupSession(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(@NotNull PlayerDeathEvent event) {
        if (flyManager.hasFlySession(event.getPlayer())) flyManager.disableFly(event.getPlayer(), false);
    }
}
