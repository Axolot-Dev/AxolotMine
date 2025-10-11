package dev.silentbit.axolotMine.models;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class Mine {

    private final String name;
    private final String worldName;
    private final Location pos1;
    private final Location pos2;
    private int resetInterval;
    private final Map<Material, Double> composition;
    private long lastReset;
    private long nextReset;
    private Location spawnPoint; // NEW: Safe spawn/teleport point

    public Mine(String name, String worldName, Location pos1, Location pos2,
                int resetInterval, Map<Material, Double> composition) {
        this.name = name;
        this.worldName = worldName;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.resetInterval = resetInterval;
        this.composition = new HashMap<>(composition);
        this.lastReset = System.currentTimeMillis();
        this.nextReset = lastReset + (resetInterval * 1000L);
        this.spawnPoint = null; // Will be set by admin using /am settp
    }

    public String getName() {
        return name;
    }

    public String getWorldName() {
        return worldName;
    }

    public Location getPos1() {
        return pos1;
    }

    public Location getPos2() {
        return pos2;
    }

    public int getResetInterval() {
        return resetInterval;
    }

    public void setResetInterval(int resetInterval) {
        this.resetInterval = resetInterval;
    }

    public Map<Material, Double> getComposition() {
        return new HashMap<>(composition);
    }

    public long getLastReset() {
        return lastReset;
    }

    public void setLastReset(long lastReset) {
        this.lastReset = lastReset;
        this.nextReset = lastReset + (resetInterval * 1000L);
    }

    public long getNextReset() {
        return nextReset;
    }

    public long getTimeUntilReset() {
        return Math.max(0, nextReset - System.currentTimeMillis());
    }

    public String getFormattedTimeUntilReset() {
        long seconds = getTimeUntilReset() / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }

    public int getBlockCount() {
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        return (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
    }

    public String getSizeString() {
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        int sizeX = maxX - minX + 1;
        int sizeY = maxY - minY + 1;
        int sizeZ = maxZ - minZ + 1;

        return sizeX + "x" + sizeY + "x" + sizeZ;
    }

    // NEW: Spawn point methods
    public Location getSpawnPoint() {
        return spawnPoint != null ? spawnPoint.clone() : null;
    }

    public void setSpawnPoint(Location spawnPoint) {
        this.spawnPoint = spawnPoint != null ? spawnPoint.clone() : null;
    }

    public boolean hasSpawnPoint() {
        return spawnPoint != null;
    }

    /**
     * Gets the safe teleport location for this mine.
     * Returns spawn point if set, otherwise returns center above mine.
     */
    public Location getSafeTeleportLocation() {
        if (spawnPoint != null) {
            return spawnPoint.clone();
        }

        // Fallback: center of mine, above the max Y
        Location center = pos1.clone().add(pos2).multiply(0.5);
        center.setY(pos2.getY() + 2);
        return center;
    }
}
