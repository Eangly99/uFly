package dev.naruto.uFly.util;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class PermissionUtil {

    private PermissionUtil() {}

    /** Returns true if the player has any of the listed permissions. */
    public static boolean hasAny(@NotNull Player player, @NotNull String... permissions) {
        for (String perm : permissions) {
            if (player.hasPermission(perm)) return true;
        }
        return false;
    }

    /**
     * Returns the highest-priority fly permission level.
     * Priority: ufly.all > ufly.trust > ufly.claimed
     *
     * @return the name of the highest permission, or null if none
     */
    public static @org.jetbrains.annotations.Nullable String getHighestFlyPermission(@NotNull Player player) {
        if (player.hasPermission("ufly.all")) return "ufly.all";
        if (player.hasPermission("ufly.trust")) return "ufly.trust";
        if (player.hasPermission("ufly.claimed")) return "ufly.claimed";
        return null;
    }
}
