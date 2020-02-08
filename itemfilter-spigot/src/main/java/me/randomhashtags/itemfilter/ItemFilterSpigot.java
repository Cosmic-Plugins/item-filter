package me.randomhashtags.itemfilter;

import org.bukkit.plugin.java.JavaPlugin;

public final class ItemFilterSpigot extends JavaPlugin {

    public static ItemFilterSpigot getPlugin;

    @Override
    public void onEnable() {
        getPlugin = this;
        saveSettings();
        getCommand("filter").setExecutor(ItemFilterAPI.INSTANCE);
        enable();
    }

    @Override
    public void onDisable() {
        disable();
    }

    private void saveSettings() {
        saveDefaultConfig();
    }

    public void enable() {
        saveSettings();
        ItemFilterAPI.INSTANCE.load();
    }
    public void disable() {
        ItemFilterAPI.INSTANCE.unload();
    }

    public void reload() {
        disable();
        enable();
    }
}
