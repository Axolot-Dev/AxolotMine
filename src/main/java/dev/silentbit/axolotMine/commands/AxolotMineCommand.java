package dev.silentbit.axolotMine.commands;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import dev.silentbit.axolotMine.AxolotMine;
import dev.silentbit.axolotMine.models.Mine;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class AxolotMineCommand implements CommandExecutor, TabCompleter {

    private final AxolotMine plugin;

    public AxolotMineCommand(AxolotMine plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Check admin permission for all commands
        if (!sender.hasPermission("axolotmine.admin")) {
            plugin.getMessageUtil().sendMessage(sender, "no-permission-admin");
            return true;
        }

        if (args.length == 0) {
            // Show help instead of opening GUI
            handleHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                return handleCreate(sender, args);
            case "reset":
                return handleReset(sender, args);
            case "resetall":
                return handleResetAll(sender);
            case "delete":
                return handleDelete(sender, args);
            case "list":
                return handleList(sender);
            case "info":
                return handleInfo(sender, args);
            case "setinterval":
                return handleSetInterval(sender, args);
            case "settp":  // NEW: Set spawn point
                return handleSetTeleportPoint(sender, args);
            case "teleport":
            case "tp":
                return handleTeleport(sender, args);
            case "composition":
            case "comp":
                return handleComposition(sender, args);
            case "reload":
                return handleReload(sender);
            case "help":
                return handleHelp(sender);
            default:
                plugin.getMessageUtil().sendMessage(sender, "unknown-command");
                return true;
        }
    }

    private boolean handleSetTeleportPoint(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageUtil().sendMessage(sender, "player-only");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            plugin.getMessageUtil().sendMessage(player, "usage-settp");
            return true;
        }

        String mineName = args[1];
        Mine mine = plugin.getMineManager().getMine(mineName);

        if (mine == null) {
            plugin.getMessageUtil().sendMessage(player, "mine-not-found",
                    Map.of("name", mineName));
            return true;
        }

        // Set the spawn point to the player's current location
        Location spawnPoint = player.getLocation();
        mine.setSpawnPoint(spawnPoint);
        plugin.getMineManager().saveMine(mine);

        plugin.getMessageUtil().sendMessage(player, "spawn-point-set",
                Map.of(
                        "name", mineName,
                        "x", String.format("%.2f", spawnPoint.getX()),
                        "y", String.format("%.2f", spawnPoint.getY()),
                        "z", String.format("%.2f", spawnPoint.getZ())
                ));

        return true;
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageUtil().sendMessage(sender, "player-only");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            plugin.getMessageUtil().sendMessage(player, "usage-create");
            return true;
        }

        String mineName = args[1];

        if (plugin.getMineManager().mineExists(mineName)) {
            plugin.getMessageUtil().sendMessage(player, "mine-already-exists",
                    Map.of("name", mineName));
            return true;
        }

        try {
            Region selection = plugin.getWorldEditHandler().getSelection(player);
            BlockVector3 min = selection.getMinimumPoint();
            BlockVector3 max = selection.getMaximumPoint();

            Location pos1 = new Location(player.getWorld(), min.x(), min.y(), min.z());
            Location pos2 = new Location(player.getWorld(), max.x(), max.y(), max.z());
            // Default composition
            Map<Material, Double> composition = new LinkedHashMap<>();
            composition.put(Material.STONE, 60.0);
            composition.put(Material.COAL_ORE, 25.0);
            composition.put(Material.IRON_ORE, 15.0);

            plugin.getMineManager().createMine(mineName, pos1, pos2, composition);
            plugin.getMessageUtil().sendMessage(player, "mine-created",
                    Map.of("name", mineName));

        } catch (IncompleteRegionException e) {
            plugin.getMessageUtil().sendMessage(player, "no-selection");
        }

        return true;
    }

    private boolean handleReset(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.getMessageUtil().sendMessage(sender, "usage-reset");
            return true;
        }

        String mineName = args[1];
        Mine mine = plugin.getMineManager().getMine(mineName);

        if (mine == null) {
            plugin.getMessageUtil().sendMessage(sender, "mine-not-found",
                    Map.of("name", mineName));
            return true;
        }

        plugin.getMineManager().resetMine(mine, true);
        plugin.getMessageUtil().sendMessage(sender, "mine-reset",
                Map.of("name", mineName));

        return true;
    }

    private boolean handleResetAll(CommandSender sender) {
        Collection<Mine> mines = plugin.getMineManager().getAllMines();

        if (mines.isEmpty()) {
            plugin.getMessageUtil().sendMessage(sender, "no-mines");
            return true;
        }

        plugin.getMineManager().resetAllMines();
        plugin.getMessageUtil().sendMessage(sender, "all-mines-reset",
                Map.of("count", String.valueOf(mines.size())));

        return true;
    }

    private boolean handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.getMessageUtil().sendMessage(sender, "usage-delete");
            return true;
        }

        String mineName = args[1];

        if (!plugin.getMineManager().mineExists(mineName)) {
            plugin.getMessageUtil().sendMessage(sender, "mine-not-found",
                    Map.of("name", mineName));
            return true;
        }

        plugin.getMineManager().deleteMine(mineName);
        plugin.getMessageUtil().sendMessage(sender, "mine-deleted",
                Map.of("name", mineName));

        return true;
    }

    private boolean handleList(CommandSender sender) {
        Collection<Mine> mines = plugin.getMineManager().getAllMines();

        if (mines.isEmpty()) {
            plugin.getMessageUtil().sendMessage(sender, "no-mines");
            return true;
        }

        plugin.getMessageUtil().sendMessage(sender, "mines-list-header");
        for (Mine mine : mines) {
            plugin.getMessageUtil().sendMessage(sender, "mines-list-entry",
                    Map.of(
                            "name", mine.getName(),
                            "world", mine.getWorldName(),
                            "size", mine.getSizeString(),
                            "blocks", String.format("%,d", mine.getBlockCount()),
                            "time", mine.getFormattedTimeUntilReset(),
                            "interval", String.valueOf(mine.getResetInterval())
                    ));
        }

        return true;
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            // Show plugin info
            plugin.getMessageUtil().sendMessage(sender, "plugin-info-header");
            plugin.getMessageUtil().sendMessage(sender, "plugin-info-version",
                    Map.of("version", plugin.getDescription().getVersion()));
            plugin.getMessageUtil().sendMessage(sender, "plugin-info-author");
            plugin.getMessageUtil().sendMessage(sender, "plugin-info-mines",
                    Map.of("count", String.valueOf(plugin.getMineManager().getAllMines().size())));
            return true;
        }

        String mineName = args[1];
        Mine mine = plugin.getMineManager().getMine(mineName);

        if (mine == null) {
            plugin.getMessageUtil().sendMessage(sender, "mine-not-found",
                    Map.of("name", mineName));
            return true;
        }

        // Display detailed mine info
        plugin.getMessageUtil().sendMessage(sender, "mine-info-header",
                Map.of("name", mine.getName()));
        plugin.getMessageUtil().sendMessage(sender, "mine-info-world",
                Map.of("world", mine.getWorldName()));
        plugin.getMessageUtil().sendMessage(sender, "mine-info-size",
                Map.of("size", mine.getSizeString()));
        plugin.getMessageUtil().sendMessage(sender, "mine-info-blocks",
                Map.of("blocks", String.format("%,d", mine.getBlockCount())));
        plugin.getMessageUtil().sendMessage(sender, "mine-info-interval",
                Map.of("interval", String.valueOf(mine.getResetInterval())));
        plugin.getMessageUtil().sendMessage(sender, "mine-info-nextreset",
                Map.of("time", mine.getFormattedTimeUntilReset()));

        // NEW: Show spawn point info
        if (mine.hasSpawnPoint()) {
            Location sp = mine.getSpawnPoint();
            plugin.getMessageUtil().sendMessage(sender, "mine-info-spawnpoint",
                    Map.of(
                            "x", String.format("%.2f", sp.getX()),
                            "y", String.format("%.2f", sp.getY()),
                            "z", String.format("%.2f", sp.getZ())
                    ));
        } else {
            plugin.getMessageUtil().sendMessage(sender, "mine-info-no-spawnpoint");
        }

        // Show composition
        plugin.getMessageUtil().sendMessage(sender, "mine-info-composition");
        for (Map.Entry<Material, Double> entry : mine.getComposition().entrySet()) {
            plugin.getMessageUtil().sendMessage(sender, "mine-info-composition-entry",
                    Map.of(
                            "material", formatMaterialName(entry.getKey()),
                            "percentage", String.format("%.1f%%", entry.getValue())
                    ));
        }

        return true;
    }

    private boolean handleSetInterval(CommandSender sender, String[] args) {
        if (args.length < 3) {
            plugin.getMessageUtil().sendMessage(sender, "usage-setinterval");
            return true;
        }

        String mineName = args[1];
        Mine mine = plugin.getMineManager().getMine(mineName);

        if (mine == null) {
            plugin.getMessageUtil().sendMessage(sender, "mine-not-found",
                    Map.of("mine", mineName));
            return true;
        }

        try {
            int interval = Integer.parseInt(args[2]);
            if (interval < 30) {
                plugin.getMessageUtil().sendMessage(sender, "interval-too-small");
                return true;
            }

            mine.setResetInterval(interval);
            plugin.getMineManager().saveMine(mine); // Save immediately
            plugin.getMineManager().scheduleReset(mine); // Reschedule

            plugin.getMessageUtil().sendMessage(sender, "interval-set",
                    Map.of("mine", mineName, "interval", String.valueOf(interval)));

        } catch (NumberFormatException e) {
            plugin.getMessageUtil().sendMessage(sender, "invalid-number");
        }

        return true;
    }

    private boolean handleTeleport(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageUtil().sendMessage(sender, "player-only");
            return true;
        }

        if (args.length < 2) {
            plugin.getMessageUtil().sendMessage(sender, "usage-teleport");
            return true;
        }

        Player player = (Player) sender;
        String mineName = args[1];
        Mine mine = plugin.getMineManager().getMine(mineName);

        if (mine == null) {
            plugin.getMessageUtil().sendMessage(player, "mine-not-found",
                    Map.of("name", mineName));
            return true;
        }

        Location center = mine.getPos1().clone().add(mine.getPos2()).multiply(0.5);
        center.setY(mine.getPos2().getY() + 2);
        player.teleport(center);

        plugin.getMessageUtil().sendMessage(player, "teleported-to-mine",
                Map.of("name", mineName));

        return true;
    }

    private boolean handleComposition(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.getMessageUtil().sendMessage(sender, "usage-composition");
            return true;
        }

        String mineName = args[1];
        Mine mine = plugin.getMineManager().getMine(mineName);

        if (mine == null) {
            plugin.getMessageUtil().sendMessage(sender, "mine-not-found",
                    Map.of("name", mineName));
            return true;
        }

        // /axolotmine comp <mine> add <material> <percentage>
        if (args.length >= 3 && args[2].equalsIgnoreCase("add")) {
            if (args.length < 5) {
                plugin.getMessageUtil().sendMessage(sender, "usage-composition-add");
                return true;
            }

            try {
                Material material = Material.valueOf(args[3].toUpperCase());
                double percentage = Double.parseDouble(args[4]);

                if (percentage < 0 || percentage > 100) {
                    plugin.getMessageUtil().sendMessage(sender, "invalid-percentage");
                    return true;
                }

                mine.getComposition().put(material, percentage);
                plugin.getMineManager().saveMine(mine);

                plugin.getMessageUtil().sendMessage(sender, "composition-added",
                        Map.of(
                                "material", formatMaterialName(material),
                                "percentage", String.format("%.1f%%", percentage),
                                "name", mineName
                        ));

            } catch (IllegalArgumentException e) {
                plugin.getMessageUtil().sendMessage(sender, "invalid-material");
            }
            return true;
        }

        // /axolotmine comp <mine> remove <material>
        if (args.length >= 3 && args[2].equalsIgnoreCase("remove")) {
            if (args.length < 4) {
                plugin.getMessageUtil().sendMessage(sender, "usage-composition-remove");
                return true;
            }

            try {
                Material material = Material.valueOf(args[3].toUpperCase());

                if (!mine.getComposition().containsKey(material)) {
                    plugin.getMessageUtil().sendMessage(sender, "material-not-in-composition");
                    return true;
                }

                mine.getComposition().remove(material);
                plugin.getMineManager().saveMine(mine);

                plugin.getMessageUtil().sendMessage(sender, "composition-removed",
                        Map.of("material", formatMaterialName(material), "name", mineName));

            } catch (IllegalArgumentException e) {
                plugin.getMessageUtil().sendMessage(sender, "invalid-material");
            }
            return true;
        }

        // Show current composition
        plugin.getMessageUtil().sendMessage(sender, "composition-header",
                Map.of("name", mineName));
        for (Map.Entry<Material, Double> entry : mine.getComposition().entrySet()) {
            plugin.getMessageUtil().sendMessage(sender, "composition-entry",
                    Map.of(
                            "material", formatMaterialName(entry.getKey()),
                            "percentage", String.format("%.1f%%", entry.getValue())
                    ));
        }

        return true;
    }

    private boolean handleReload(CommandSender sender) {
        plugin.getConfigManager().loadConfigs();
        plugin.getMineManager().loadMines();
        plugin.getMessageUtil().sendMessage(sender, "config-reloaded");

        return true;
    }

    private boolean handleHelp(CommandSender sender) {
        plugin.getMessageUtil().sendMessage(sender, "help-header");
        plugin.getMessageUtil().sendMessage(sender, "help-create");
        plugin.getMessageUtil().sendMessage(sender, "help-reset");
        plugin.getMessageUtil().sendMessage(sender, "help-resetall");
        plugin.getMessageUtil().sendMessage(sender, "help-delete");
        plugin.getMessageUtil().sendMessage(sender, "help-list");
        plugin.getMessageUtil().sendMessage(sender, "help-info");
        plugin.getMessageUtil().sendMessage(sender, "help-setinterval");
        plugin.getMessageUtil().sendMessage(sender, "help-settp");  // NEW
        plugin.getMessageUtil().sendMessage(sender, "help-teleport");
        plugin.getMessageUtil().sendMessage(sender, "help-composition");
        plugin.getMessageUtil().sendMessage(sender, "help-reload");
        plugin.getMessageUtil().sendMessage(sender, "help-footer");

        return true;
    }

    private String formatMaterialName(Material material) {
        String[] parts = material.name().toLowerCase().split("_");
        return Arrays.stream(parts)
                .map(part -> Character.toUpperCase(part.charAt(0)) + part.substring(1))
                .collect(Collectors.joining(" "));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        // Only show tab completions to admins
        if (!sender.hasPermission("axolotmine.admin")) {
            return new ArrayList<>();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("create", "reset", "resetall", "delete",
                    "list", "info", "setinterval", "settp", "teleport", "tp",
                    "composition", "comp", "reload", "help"));
        } else if (args.length == 2) {
            String subCmd = args[0].toLowerCase();
            if (subCmd.equals("reset") || subCmd.equals("delete") || subCmd.equals("info")
                    || subCmd.equals("setinterval") || subCmd.equals("settp")  // NEW
                    || subCmd.equals("teleport") || subCmd.equals("tp")
                    || subCmd.equals("composition") || subCmd.equals("comp")) {
                completions.addAll(plugin.getMineManager().getAllMines().stream()
                        .map(Mine::getName)
                        .collect(Collectors.toList()));
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("composition") || args[0].equalsIgnoreCase("comp")) {
                completions.addAll(Arrays.asList("add", "remove"));
            }
        } else if (args.length == 4) {
            if ((args[0].equalsIgnoreCase("composition") || args[0].equalsIgnoreCase("comp"))
                    && (args[2].equalsIgnoreCase("add") || args[2].equalsIgnoreCase("remove"))) {
                // Suggest common materials
                completions.addAll(Arrays.asList("STONE", "COAL_ORE", "IRON_ORE",
                        "GOLD_ORE", "DIAMOND_ORE", "EMERALD_ORE", "COPPER_ORE"));
            }
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
