package dev.silentbit.axolotMine.utils;

import org.bukkit.Location;
import org.bukkit.World;

public class ConfigUtil {

    /**
     * Converts a location to a simple string (without yaw/pitch)
     * Format: "x,y,z"
     */
    public static String locationToString(Location loc) {
        return loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    /**
     * Converts a location to a full string (with yaw/pitch)
     * Format: "x,y,z,yaw,pitch"
     */
    public static String locationToFullString(Location loc) {
        return loc.getX() + "," +
                loc.getY() + "," +
                loc.getZ() + "," +
                loc.getYaw() + "," +
                loc.getPitch();
    }

    /**
     * Converts a simple string to a location (without yaw/pitch)
     */
    public static Location stringToLocation(String str, World world) {
        if (str == null || str.isEmpty()) return null;

        try {
            String[] parts = str.split(",");
            if (parts.length >= 3) {
                return new Location(world,
                        Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]));
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    /**
     * Converts a full string to a location (with yaw/pitch)
     */
    public static Location stringToFullLocation(String str, World world) {
        if (str == null || str.isEmpty()) return null;

        try {
            String[] parts = str.split(",");
            if (parts.length >= 5) {
                return new Location(world,
                        Double.parseDouble(parts[0]),
                        Double.parseDouble(parts[1]),
                        Double.parseDouble(parts[2]),
                        Float.parseFloat(parts[3]),
                        Float.parseFloat(parts[4]));
            } else if (parts.length >= 3) {
                // Fallback to simple location
                return new Location(world,
                        Double.parseDouble(parts[0]),
                        Double.parseDouble(parts[1]),
                        Double.parseDouble(parts[2]));
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }
}
