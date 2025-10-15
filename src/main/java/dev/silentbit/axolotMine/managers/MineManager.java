package dev.silentbit.axolotMine.managers;

import dev.silentbit.axolotMine.AxolotMine;
import dev.silentbit.axolotMine.models.Mine;
import dev.silentbit.axolotMine.tasks.MineResetTask;
import dev.silentbit.axolotMine.utils.ConfigUtil;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MineManager {

    private final AxolotMine plugin;
    private final Map<String, Mine> mines;
    private final Map<String, ScheduledTask> resetTasks;
    private final File minesFolder;

    public MineManager(AxolotMine plugin) {
        this.plugin = plugin;
        this.mines = new ConcurrentHashMap<>();
        this.resetTasks = new ConcurrentHashMap<>();
        this.minesFolder = new File(plugin.getDataFolder(), "mines");

        if (!minesFolder.exists()) {
            minesFolder.mkdirs();
        }
    }

    public void loadMines() {
        mines.clear();

        File[] files = minesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            plugin.getLogger().info("No mines found to load.");
            return;
        }

        int loaded = 0;
        for (File file : files) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                Mine mine = loadMineFromConfig(config);

                if (mine != null) {
                    mines.put(mine.getName(), mine);
                    scheduleReset(mine);
                    loaded++;
                    plugin.getLogger().info("Loaded mine: " + mine.getName());
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load mine from " + file.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        plugin.getLogger().info("Loaded " + loaded + " mine(s) successfully!");
    }

    private Mine loadMineFromConfig(YamlConfiguration config) {
        String name = config.getString("name");
        if (name == null) {
            plugin.getLogger().warning("Mine has no name in config!");
            return null;
        }

        String worldName = config.getString("region.world");
        if (worldName == null) {
            plugin.getLogger().warning("Mine " + name + " has no world specified!");
            return null;
        }

        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("World " + worldName + " not found for mine " + name);
            return null;
        }

        // Load positions
        String pos1Str = config.getString("region.pos1");
        String pos2Str = config.getString("region.pos2");

        if (pos1Str == null || pos2Str == null) {
            plugin.getLogger().warning("Mine " + name + " missing position data!");
            return null;
        }

        String[] pos1Parts = pos1Str.split(",");
        String[] pos2Parts = pos2Str.split(",");

        if (pos1Parts.length < 3 || pos2Parts.length < 3) {
            plugin.getLogger().warning("Mine " + name + " has invalid position format!");
            return null;
        }

        Location pos1 = new Location(world,
                Integer.parseInt(pos1Parts[0]),
                Integer.parseInt(pos1Parts[1]),
                Integer.parseInt(pos1Parts[2]));

        Location pos2 = new Location(world,
                Integer.parseInt(pos2Parts[0]),
                Integer.parseInt(pos2Parts[1]),
                Integer.parseInt(pos2Parts[2]));

        // Load reset interval
        int resetInterval = config.getInt("reset-interval",
                plugin.getConfigManager().getDefaultResetInterval());

        // Load composition
        Map<Material, Double> composition = new HashMap<>();
        ConfigurationSection compSection = config.getConfigurationSection("composition");

        if (compSection != null) {
            for (String key : compSection.getKeys(false)) {
                try {
                    Material material = Material.valueOf(key.toUpperCase());
                    double percentage = compSection.getDouble(key);
                    composition.put(material, percentage);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material in mine " + name + ": " + key);
                }
            }
        }

        if (composition.isEmpty()) {
            plugin.getLogger().warning("Mine " + name + " has no composition! Using defaults.");
            composition.put(Material.STONE, 100.0);
        }

        // Create mine object
        Mine mine = new Mine(name, worldName, pos1, pos2, resetInterval, composition);

        // IMPORTANT: Load last reset time from config
        long lastReset = config.getLong("last-reset", System.currentTimeMillis());
        mine.setLastReset(lastReset);

        // Load spawn point if it exists
        if (config.contains("spawn-point")) {
            String spawnStr = config.getString("spawn-point");
            if (spawnStr != null && !spawnStr.isEmpty()) {
                Location spawnPoint = ConfigUtil.stringToFullLocation(spawnStr, world);
                if (spawnPoint != null) {
                    mine.setSpawnPoint(spawnPoint);
                }
            }
        }

        return mine;
    }

    public void saveMine(Mine mine) {
        File file = new File(minesFolder, mine.getName() + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        config.set("name", mine.getName());
        config.set("region.world", mine.getWorldName());
        config.set("region.pos1", ConfigUtil.locationToString(mine.getPos1()));
        config.set("region.pos2", ConfigUtil.locationToString(mine.getPos2()));
        config.set("reset-interval", mine.getResetInterval());

        // IMPORTANT: Save last reset timestamp
        config.set("last-reset", mine.getLastReset());

        // Save spawn point if set
        if (mine.hasSpawnPoint()) {
            config.set("spawn-point", ConfigUtil.locationToFullString(mine.getSpawnPoint()));
        }

        // Save composition
        ConfigurationSection compSection = config.createSection("composition");
        for (Map.Entry<Material, Double> entry : mine.getComposition().entrySet()) {
            compSection.set(entry.getKey().name(), entry.getValue());
        }

        try {
            config.save(file);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save mine " + mine.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void createMine(String name, Location pos1, Location pos2, Map<Material, Double> composition) {
        int resetInterval = plugin.getConfigManager().getDefaultResetInterval();
        Mine mine = new Mine(name, pos1.getWorld().getName(), pos1, pos2, resetInterval, composition);

        mines.put(name, mine);
        saveMine(mine);

        // Initial fill
        resetMine(mine, false);

        // Schedule automatic resets
        scheduleReset(mine);
    }

    public void resetMine(Mine mine, boolean async) {
        MineResetTask task = new MineResetTask(plugin, mine);

        if (async) {
            // Use region scheduler for Folia-safe execution
            Location center = mine.getPos1().clone().add(mine.getPos2()).multiply(0.5);
            plugin.getServer().getRegionScheduler().run(plugin, center, scheduledTask -> {
                task.run();
            });
        } else {
            task.run();
        }

        // Update last reset time and save
        mine.setLastReset(System.currentTimeMillis());
        saveMine(mine);
    }

    public void scheduleReset(Mine mine) {
        // Cancel existing task if any
        ScheduledTask existingTask = resetTasks.get(mine.getName());
        if (existingTask != null) {
            existingTask.cancel();
        }

        // Calculate time until next reset
        long currentTime = System.currentTimeMillis();
        long timeSinceLastReset = currentTime - mine.getLastReset();
        long resetIntervalMs = mine.getResetInterval() * 1000L;
        long timeUntilNextReset = resetIntervalMs - timeSinceLastReset;

        // If reset time has already passed (server was offline), reset immediately
        if (timeUntilNextReset <= 0) {
            plugin.getLogger().info("Mine '" + mine.getName() + "' was due for reset during downtime. Resetting now...");
            resetMine(mine, true);

            // Schedule the next reset after this one
            scheduleReset(mine);
            return;
        }

        // Schedule reset at the calculated time
        Location center = mine.getPos1().clone().add(mine.getPos2()).multiply(0.5);

        // Convert milliseconds to ticks (1 second = 20 ticks)
        long delayTicks = timeUntilNextReset / 50; // 50ms per tick

        ScheduledTask task = plugin.getServer().getRegionScheduler().runDelayed(
                plugin,
                center,
                scheduledTask -> {
                    resetMine(mine, false);
                    scheduleReset(mine); // Reschedule for next reset
                },
                delayTicks
        );

        resetTasks.put(mine.getName(), task);

        plugin.getLogger().info("Scheduled reset for mine '" + mine.getName() + "' in " +
                (timeUntilNextReset / 1000) + " seconds");
    }

    public void deleteMine(String name) {
        Mine mine = mines.remove(name);
        if (mine != null) {
            ScheduledTask task = resetTasks.remove(name);
            if (task != null) {
                task.cancel();
            }

            File file = new File(minesFolder, name + ".yml");
            if (file.exists()) {
                file.delete();
            }
        }
    }

    public Mine getMine(String name) {
        return mines.get(name);
    }

    public Collection<Mine> getAllMines() {
        return new ArrayList<>(mines.values());
    }

    public boolean mineExists(String name) {
        return mines.containsKey(name);
    }

    public void resetAllMines() {
        for (Mine mine : mines.values()) {
            resetMine(mine, true);
        }
    }

    public void shutdown() {
        // Save all mines before shutdown
        plugin.getLogger().info("Saving all mines...");
        for (Mine mine : mines.values()) {
            saveMine(mine);
        }

        // Cancel all scheduled tasks
        for (ScheduledTask task : resetTasks.values()) {
            task.cancel();
        }
        resetTasks.clear();

        plugin.getLogger().info("All mines saved and tasks cancelled!");
    }
}
