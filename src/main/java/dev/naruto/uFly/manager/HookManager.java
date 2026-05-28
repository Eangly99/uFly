package dev.naruto.uFly.manager;

import dev.naruto.uFly.hook.CelestCombatHook;
import dev.naruto.uFly.hook.PlotSquaredHook;
import dev.naruto.uFly.hook.PluginHook;
import dev.naruto.uFly.hook.WorldGuardHook;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HookManager {

    private final List<PluginHook> hooks = new ArrayList<>();
    private final PlotSquaredHook plotSquaredHook;
    private final CelestCombatHook celestCombatHook;
    private final WorldGuardHook worldGuardHook;

    public HookManager(@NotNull JavaPlugin plugin) {
        plotSquaredHook = new PlotSquaredHook(plugin);
        celestCombatHook = new CelestCombatHook(plugin);
        worldGuardHook = new WorldGuardHook(plugin);

        hooks.add(plotSquaredHook);
        hooks.add(celestCombatHook);
        hooks.add(worldGuardHook);
    }

    /** Attempts to enable all registered hooks, skipping those whose plugins are absent. */
    public void registerAll() {
        for (PluginHook hook : hooks) {
            hook.onEnable();
        }
    }

    /** Disables all hooks gracefully. */
    public void disableAll() {
        for (PluginHook hook : hooks) {
            if (hook.isEnabled()) hook.onDisable();
        }
    }

    public @NotNull PlotSquaredHook getPlotSquaredHook() { return plotSquaredHook; }
    public @NotNull CelestCombatHook getCelestCombatHook() { return celestCombatHook; }
    public @NotNull WorldGuardHook getWorldGuardHook() { return worldGuardHook; }
}
