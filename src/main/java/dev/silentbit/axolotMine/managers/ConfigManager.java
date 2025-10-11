package dev.silentbit.axolotMine.managers;

import dev.silentbit.axolotMine.AxolotMine;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigManager {

    private final AxolotMine plugin;
    private FileConfiguration config;
    private FileConfiguration messages;

    public ConfigManager(AxolotMine plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        // Load config.yml
        plugin.saveDefaultConfig();
        config = plugin.getConfig();

        // Load messages.yml
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public int getDefaultResetInterval() {
        return config.getInt("default-reset-interval", 600);
    }
}
