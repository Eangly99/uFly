package dev.naruto.uFly.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class MessageUtil {

    private MessageUtil() {}

    /**
     * Sends a message to a player — as an action bar if actionbar is enabled, otherwise as chat.
     *
     * @param player    the recipient
     * @param component the message component
     * @param actionBar if true, use the action bar
     */
    public static void send(@NotNull Player player, @NotNull Component component, boolean actionBar) {
        if (actionBar) {
            player.sendActionBar(component);
        } else {
            player.sendMessage(component);
        }
    }

    /**
     * Sends a chat message unconditionally.
     */
    public static void sendChat(@NotNull Player player, @NotNull Component component) {
        player.sendMessage(component);
    }
}
