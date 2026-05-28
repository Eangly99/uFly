package dev.naruto.uFly.model;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Immutable record representing a player's active fly session.
 *
 * @param playerUuid   The UUID of the player.
 * @param autoEnabled  Whether fly was auto-enabled by the plugin (plot enter).
 * @param worldName    The world in which fly was granted.
 */
public record FlySession(
        @NotNull UUID playerUuid,
        boolean autoEnabled,
        @NotNull String worldName
) {
    public FlySession withAutoEnabled(boolean autoEnabled) {
        return new FlySession(playerUuid, autoEnabled, worldName);
    }
}
