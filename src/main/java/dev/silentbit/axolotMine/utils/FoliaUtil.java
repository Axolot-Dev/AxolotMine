package dev.silentbit.axolotMine.utils;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public class FoliaUtil {

    public static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void runTask(Plugin plugin, Location location, Runnable task) {
        if (isFolia()) {
            plugin.getServer().getRegionScheduler().run(plugin, location, scheduledTask -> task.run());
        } else {
            plugin.getServer().getScheduler().runTask(plugin, task);
        }
    }
}
