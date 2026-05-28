package dev.naruto.uFly.command;

import dev.naruto.uFly.UFlyPlugin;
import dev.naruto.uFly.config.ConfigManager;
import dev.naruto.uFly.manager.FlyManager;
import dev.naruto.uFly.util.MessageUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UFlyCommand implements CommandExecutor, TabCompleter {

    private final UFlyPlugin plugin;
    private final FlyManager flyManager;
    private final ConfigManager configManager;
    private static final MiniMessage MINI = MiniMessage.miniMessage();

    public UFlyCommand(@NotNull UFlyPlugin plugin,
                       @NotNull FlyManager flyManager,
                       @NotNull ConfigManager configManager) {
        this.plugin = plugin;
        this.flyManager = flyManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MINI.deserialize("<red>Only players can use this command."));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("ufly.admin")) {
                MessageUtil.sendChat(player, configManager.getMessage("no-permission"));
                return true;
            }
            configManager.reload();
            MessageUtil.sendChat(player, MINI.deserialize(
                    configManager.getString("messages.prefix") + "<green>Config reloaded."));
            return true;
        }

        boolean actionBar = configManager.getBoolean("settings.actionbar-notifications");
        FlyManager.ToggleResult result = flyManager.toggleFly(player, false);

        switch (result) {
            case ENABLED ->
                MessageUtil.send(player, configManager.getMessage("fly-enabled"), actionBar);
            case DISABLED ->
                MessageUtil.send(player, configManager.getMessage("fly-disabled"), actionBar);
            case DENIED_NO_PERMISSION ->
                MessageUtil.sendChat(player, configManager.getMessage("no-permission"));
            case DENIED_COMBAT ->
                MessageUtil.sendChat(player, configManager.getMessage("in-combat"));
            case DENIED_REGION ->
                MessageUtil.sendChat(player, configManager.getMessage("region-denied"));
            case DENIED_PLOT ->
                MessageUtil.sendChat(player, configManager.getMessage("no-permission"));
            case DENIED_WORLD ->
                MessageUtil.sendChat(player, configManager.getMessage("world-denied"));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("ufly.admin")) {
            return List.of("reload");
        }
        return List.of();
    }
}
