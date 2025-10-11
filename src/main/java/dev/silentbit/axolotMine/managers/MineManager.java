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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
        if (files == null) return;

        for (File file : files) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                Mine mine = loadMineFromConfig(config);

                if (mine != null) {
                    mines.put(mine.getName(), mine);
                    scheduleReset(mine);
                    plugin.getLogger().info("Loaded mine: " + mine.getName());
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load mine from " + file.getName() + ": " + e.getMessage());
            }
        }

        plugin.getLogger().info("Loaded " + mines.size() + " mine(s)");
    }

    private Mine loadMineFromConfig(YamlConfiguration config) {
        String name = config.getString("name");
        String worldName = config.getString("region.world");

        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("World " + worldName + " not found for mine " + name);
            return null;
        }

        String[] pos1Parts = config.getString("region.pos1").split(",");
        String[] pos2Parts = config.getString("region.pos2").split(",");

        Location pos1 = new Location(world,
                Integer.parseInt(pos1Parts[0]),
                Integer.parseInt(pos1Parts[1]),
                Integer.parseInt(pos1Parts[2]));

        Location pos2 = new Location(world,
                Integer.parseInt(pos2Parts[0]),
                Integer.parseInt(pos2Parts[1]),
                Integer.parseInt(pos2Parts[2]));

        int resetInterval = config.getInt("reset-interval",
                plugin.getConfigManager().getDefaultResetInterval());

        Map<Material, Double> composition = new HashMap<>();
        ConfigurationSection compSection = config.getConfigurationSection("composition");

        if (compSection != null) {
            for (String key : compSection.getKeys(false)) {
                try {
                    Material material = Material.valueOf(key.toUpperCase());
                    double percentage = compSection.getDouble(key);
                    composition.put(material, percentage);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material: " + key);
                }
            }
        }

        Mine mine = new Mine(name, worldName, pos1, pos2, resetInterval, composition);

        // NEW: Load spawn point if it exists
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

        // NEW: Save spawn point if set
        if (mine.hasSpawnPoint()) {
            config.set("spawn-point", ConfigUtil.locationToFullString(mine.getSpawnPoint()));
        }

        ConfigurationSection compSection = config.createSection("composition");
        for (Map.Entry<Material, Double> entry : mine.getComposition().entrySet()) {
            compSection.set(entry.getKey().name(), entry.getValue());
        }

        try {
            config.save(file);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save mine " + mine.getName() + ": " + e.getMessage());
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

        mine.setLastReset(System.currentTimeMillis());
    }

    public void scheduleReset(Mine mine) {
        // Cancel existing task if any
        ScheduledTask existingTask = resetTasks.get(mine.getName());
        if (existingTask != null) {
            existingTask.cancel();
        }

        // Schedule new reset task
        Location center = mine.getPos1().clone().add(mine.getPos2()).multiply(0.5);

        ScheduledTask task = plugin.getServer().getRegionScheduler().runDelayed(
                plugin,
                center,
                scheduledTask -> {
                    resetMine(mine, false);
                    scheduleReset(mine); // Reschedule
                },
                mine.getResetInterval() * 20L // Convert seconds to ticks
        );

        resetTasks.put(mine.getName(), task);
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

    public void shutdown() {
        for (ScheduledTask task : resetTasks.values()) {
            task.cancel();
        }
        resetTasks.clear();
    }

    public void resetAllMines() {
        for (Mine mine : mines.values()) {
            resetMine(mine, true);
        }
    }

    public List<Mine> getMinesByWorld(String worldName) {
        return mines.values().stream()
                .filter(mine -> mine.getWorldName().equals(worldName))
                .collect(Collectors.toList());
    }

    public Mine getNearestMine(org.bukkit.Location location) {
        return mines.values().stream()
                .filter(mine -> mine.getWorldName().equals(location.getWorld().getName()))
                .min((m1, m2) -> {
                    double dist1 = location.distance(
                            m1.getPos1().clone().add(m1.getPos2()).multiply(0.5));
                    double dist2 = location.distance(
                            m2.getPos1().clone().add(m2.getPos2()).multiply(0.5));
                    return Double.compare(dist1, dist2);
                })
                .orElse(null);
    }
}
