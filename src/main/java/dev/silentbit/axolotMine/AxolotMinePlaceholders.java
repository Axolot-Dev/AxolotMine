package dev.silentbit.axolotMine;

import dev.silentbit.axolotMine.models.Mine;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

public class AxolotMinePlaceholders extends PlaceholderExpansion {

    private final AxolotMine plugin;

    public AxolotMinePlaceholders(AxolotMine plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "axolotmine";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {

        // %axolotmine_total%
        if (params.equals("total")) {
            return String.valueOf(plugin.getMineManager().getAllMines().size());
        }

        // %axolotmine_active%
        if (params.equals("active")) {
            long active = plugin.getMineManager().getAllMines().stream()
                    .filter(m -> m.getTimeUntilReset() > 0)
                    .count();
            return String.valueOf(active);
        }

        // %axolotmine_resetting%
        if (params.equals("resetting")) {
            long resetting = plugin.getMineManager().getAllMines().stream()
                    .filter(m -> m.getTimeUntilReset() <= 0)
                    .count();
            return String.valueOf(resetting);
        }

        // %axolotmine_total_blocks%
        if (params.equals("total_blocks")) {
            int totalBlocks = plugin.getMineManager().getAllMines().stream()
                    .mapToInt(Mine::getBlockCount)
                    .sum();
            return String.format("%,d", totalBlocks);
        }

        // Mine-specific placeholders
        if (params.startsWith("mine_")) {
            String[] parts = params.split("_", 3);
            if (parts.length < 3) return null;

            String mineName = parts[1];
            String attribute = parts[2];

            Mine mine = plugin.getMineManager().getMine(mineName);
            if (mine == null) return "N/A";

            return getMineAttribute(mine, attribute);
        }

        // Closest mine to player
        if (player != null && params.startsWith("closest_")) {
            String attribute = params.substring(8);
            Mine closest = findClosestMine(player);
            if (closest == null) return "N/A";

            return getMineAttribute(closest, attribute);
        }

        // List all mines
        if (params.equals("list")) {
            Collection<Mine> mines = plugin.getMineManager().getAllMines();
            if (mines.isEmpty()) return "None";

            StringBuilder list = new StringBuilder();
            for (Mine mine : mines) {
                if (list.length() > 0) list.append(", ");
                list.append(mine.getName());
            }
            return list.toString();
        }

        // Average reset time
        if (params.equals("avg_reset_time")) {
            double avg = plugin.getMineManager().getAllMines().stream()
                    .mapToInt(Mine::getResetInterval)
                    .average()
                    .orElse(0);
            return String.format("%.0f", avg);
        }

        return null;
    }

    private String getMineAttribute(Mine mine, String attribute) {
        switch (attribute.toLowerCase()) {
            case "nextreset":
            case "next_reset":
                return mine.getFormattedTimeUntilReset();

            case "nextreset_seconds":
                return String.valueOf(mine.getTimeUntilReset() / 1000);

            case "nextreset_minutes":
                return String.valueOf(mine.getTimeUntilReset() / 60000);

            case "interval":
                return String.valueOf(mine.getResetInterval());

            case "world":
                return mine.getWorldName();

            case "size":
                return mine.getSizeString();

            case "blocks":
            case "block_count":
                return String.format("%,d", mine.getBlockCount());

            case "composition_count":
                return String.valueOf(mine.getComposition().size());

            case "pos1":
                return String.format("%d, %d, %d",
                        mine.getPos1().getBlockX(),
                        mine.getPos1().getBlockY(),
                        mine.getPos1().getBlockZ());

            case "pos2":
                return String.format("%d, %d, %d",
                        mine.getPos2().getBlockX(),
                        mine.getPos2().getBlockY(),
                        mine.getPos2().getBlockZ());

            case "status":
                long timeLeft = mine.getTimeUntilReset();
                if (timeLeft < 60000) return "Resetting Soon";
                if (timeLeft < 300000) return "Active";
                return "Stable";

            case "progress":
                double progressPercentage = (double) mine.getTimeUntilReset() /
                        (mine.getResetInterval() * 1000L) * 100;
                return String.format("%.0f%%", progressPercentage);

            case "progress_bar":
                return createTextProgressBar(mine);

            // Top material in composition
            case "top_material":
                return mine.getComposition().entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(e -> formatMaterialName(e.getKey()))
                        .orElse("None");

            case "top_material_percent":
                return mine.getComposition().entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(e -> String.format("%.1f%%", e.getValue()))
                        .orElse("0%");

            default:
                // Check if requesting specific material percentage
                // %axolotmine_mine_MyMine_STONE%
                try {
                    Material material = Material.valueOf(attribute.toUpperCase());
                    Double materialPercentage = mine.getComposition().get(material);
                    if (materialPercentage != null) {
                        return String.format("%.1f%%", materialPercentage);
                    }
                } catch (IllegalArgumentException ignored) {}

                return null;
        }
    }

    private Mine findClosestMine(Player player) {
        return plugin.getMineManager().getAllMines().stream()
                .filter(mine -> mine.getWorldName().equals(player.getWorld().getName()))
                .min((m1, m2) -> {
                    double dist1 = player.getLocation().distance(
                            m1.getPos1().clone().add(m1.getPos2()).multiply(0.5));
                    double dist2 = player.getLocation().distance(
                            m2.getPos1().clone().add(m2.getPos2()).multiply(0.5));
                    return Double.compare(dist1, dist2);
                })
                .orElse(null);
    }

    private String createTextProgressBar(Mine mine) {
        int totalSeconds = mine.getResetInterval();
        long remainingSeconds = mine.getTimeUntilReset() / 1000;

        double percentage = (double) remainingSeconds / totalSeconds;
        int filled = (int) (10 * percentage);
        int empty = 10 - filled;

        return "█".repeat(Math.max(0, filled)) +
                "░".repeat(Math.max(0, empty));
    }

    private String formatMaterialName(Material material) {
        String[] parts = material.name().toLowerCase().split("_");
        StringBuilder formatted = new StringBuilder();
        for (String part : parts) {
            formatted.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.substring(1))
                    .append(" ");
        }
        return formatted.toString().trim();
    }
}
