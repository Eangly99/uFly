package dev.naruto.uFly;

import dev.naruto.uFly.command.UFlyCommand;
import dev.naruto.uFly.config.ConfigManager;
import dev.naruto.uFly.listener.FlyListener;
import dev.naruto.uFly.listener.PlotListener;
import dev.naruto.uFly.manager.FlyManager;
import dev.naruto.uFly.manager.HookManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class UFlyPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private HookManager hookManager;
    private FlyManager flyManager;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        configManager.load();

        this.hookManager = new HookManager(this);
        hookManager.registerAll();

        this.flyManager = new FlyManager(this, configManager, hookManager);

        getServer().getPluginManager().registerEvents(new FlyListener(this, flyManager), this);

        if (hookManager.getPlotSquaredHook().isEnabled()) {
            getServer().getPluginManager().registerEvents(new PlotListener(this, flyManager), this);
        }

        UFlyCommand cmd = new UFlyCommand(this, flyManager, configManager);
        var command = getCommand("ufly");
        if (command != null) {
            command.setExecutor(cmd);
            command.setTabCompleter(cmd);
        }

        getLogger().info("uFly enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (flyManager != null) flyManager.disableAll();
        if (hookManager != null) hookManager.disableAll();
        getLogger().info("uFly disabled.");
    }

    public @NotNull ConfigManager getConfigManager() { return configManager; }
    public @NotNull HookManager getHookManager() { return hookManager; }
    public @NotNull FlyManager getFlyManager() { return flyManager; }
}
