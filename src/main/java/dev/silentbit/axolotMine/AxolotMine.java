package dev.silentbit.axolotMine;

import dev.silentbit.axolotMine.commands.AxolotMineCommand;
import dev.silentbit.axolotMine.managers.*;
import dev.silentbit.axolotMine.utils.MessageUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

public final class  AxolotMine extends JavaPlugin {

    private static AxolotMine instance;
    private MineManager mineManager;
    private ConfigManager configManager;
    private WorldEditHandler worldEditHandler;
    private WorldsHandler worldsHandler;
    private MessageUtil messageUtil;
    private MiniMessage miniMessage;

    @Override
    public void onEnable() {
        instance = this;

        // Display ASCII banner
        displayBanner();

        // Initialize MiniMessage
        miniMessage = MiniMessage.miniMessage();

        // Check for WorldEdit dependency
        if (getServer().getPluginManager().getPlugin("WorldEdit") == null) {
            getLogger().severe("╔════════════════════════════════════════╗");
            getLogger().severe("║  WorldEdit is required! Disabling...   ║");
            getLogger().severe("╚════════════════════════════════════════╝");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize managers
        getLogger().info("┌─────────────────────────────────────────┐");
        getLogger().info("│  Initializing AxolotMine components...  │");
        getLogger().info("└─────────────────────────────────────────┘");

        configManager = new ConfigManager(this);
        configManager.loadConfigs();
        getLogger().info("  ✓ Configuration Manager loaded");

        messageUtil = new MessageUtil(this);
        worldEditHandler = new WorldEditHandler(this);
        getLogger().info("  ✓ WorldEdit Handler initialized");

        // Optional: Worlds API integration
        if (getServer().getPluginManager().getPlugin("Worlds") != null) {
            worldsHandler = new WorldsHandler(this);
            getLogger().info("  ✓ Worlds API integration enabled");
        }

        mineManager = new MineManager(this);
        getLogger().info("  ✓ Mine Manager initialized");

        // Register commands
        getCommand("axolotmine").setExecutor(new AxolotMineCommand(this));
        getLogger().info("  ✓ Commands registered");

        // Register PlaceholderAPI expansion if available
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new AxolotMinePlaceholders(this).register();
            getLogger().info("  ✓ PlaceholderAPI integration enabled");
        }

        // Load all mines
        mineManager.loadMines();

        getLogger().info("┌─────────────────────────────────────────┐");
        getLogger().info("│  AxolotMine enabled successfully!       │");
        getLogger().info("└─────────────────────────────────────────┘");
    }

    @Override
    public void onDisable() {
        // Cancel all scheduled tasks and save data
        if (mineManager != null) {
            getLogger().info("Shutting down mine reset tasks...");
            mineManager.shutdown();
        }

        getLogger().info("╔════════════════════════════════════════╗");
        getLogger().info("║  AxolotMine has been disabled!         ║");
        getLogger().info("║  Thank you for using AxolotMine!       ║");
        getLogger().info("╚════════════════════════════════════════╝");
    }

    private void displayBanner() {
        getLogger().info("╔════════════════════════════════════════════════════════════╗");
        getLogger().info("║                                                            ║");
        getLogger().info("║      █████╗ ██╗  ██╗ ██████╗ ██╗      ██████╗ ████████╗   ║");
        getLogger().info("║     ██╔══██╗╚██╗██╔╝██╔═══██╗██║     ██╔═══██╗╚══██╔══╝   ║");
        getLogger().info("║     ███████║ ╚███╔╝ ██║   ██║██║     ██║   ██║   ██║      ║");
        getLogger().info("║     ██╔══██║ ██╔██╗ ██║   ██║██║     ██║   ██║   ██║      ║");
        getLogger().info("║     ██║  ██║██╔╝ ██╗╚██████╔╝███████╗╚██████╔╝   ██║      ║");
        getLogger().info("║     ╚═╝  ╚═╝╚═╝  ╚═╝ ╚═════╝ ╚══════╝ ╚═════╝    ╚═╝      ║");
        getLogger().info("║                                                            ║");
        getLogger().info("║            ███╗   ███╗██╗███╗   ██╗███████╗                ║");
        getLogger().info("║            ████╗ ████║██║████╗  ██║██╔════╝                ║");
        getLogger().info("║            ██╔████╔██║██║██╔██╗ ██║█████╗                  ║");
        getLogger().info("║            ██║╚██╔╝██║██║██║╚██╗██║██╔══╝                  ║");
        getLogger().info("║            ██║ ╚═╝ ██║██║██║ ╚████║███████╗                ║");
        getLogger().info("║            ╚═╝     ╚═╝╚═╝╚═╝  ╚═══╝╚══════╝                ║");
        getLogger().info("║                                                            ║");
        getLogger().info("╠════════════════════════════════════════════════════════════╣");
        getLogger().info("║                                                            ║");
        getLogger().info("║  Version: " + String.format("%-47s", getDescription().getVersion()) + "║");
        getLogger().info("║  Author:  " + String.format("%-47s", "SilentBit Development Team") + "║");
        getLogger().info("║  Type:    " + String.format("%-47s", "Folia-Safe Mine Reset System") + "║");
        getLogger().info("║  Mode:    " + String.format("%-47s", "Command-Based | Admin-Only") + "║");
        getLogger().info("║                                                            ║");
        getLogger().info("╚════════════════════════════════════════════════════════════╝");
    }

    public static AxolotMine getInstance() {
        return instance;
    }

    public MineManager getMineManager() {
        return mineManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public WorldEditHandler getWorldEditHandler() {
        return worldEditHandler;
    }

    public WorldsHandler getWorldsHandler() {
        return worldsHandler;
    }

    public MessageUtil getMessageUtil() {
        return messageUtil;
    }

    public MiniMessage getMiniMessage() {
        return miniMessage;
    }
}
