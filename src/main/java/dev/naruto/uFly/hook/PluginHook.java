package dev.naruto.uFly.hook;

/**
 * Common interface for all soft-dependency hooks.
 */
public interface PluginHook {
    /** @return true if the backing plugin is present and successfully hooked. */
    boolean isEnabled();

    /** Called when the hook should initialise itself. */
    void onEnable();

    /** Called when the hook should clean up resources. */
    void onDisable();
}
