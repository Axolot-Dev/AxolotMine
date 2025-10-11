package dev.silentbit.axolotMine.tasks;

import dev.silentbit.axolotMine.AxolotMine;
import dev.silentbit.axolotMine.models.Mine;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

public class MineResetTask implements Runnable {

    private final AxolotMine plugin;
    private final Mine mine;

    public MineResetTask(AxolotMine plugin, Mine mine) {
        this.plugin = plugin;
        this.mine = mine;
    }

    @Override
    public void run() {
        // STEP 1: Teleport players out IMMEDIATELY (no warning)
        teleportPlayersOutOfMine();

        // STEP 2: Reset the mine blocks
        resetMineBlocks();

        plugin.getLogger().info("Mine '" + mine.getName() + "' has been reset!");
    }

    private void teleportPlayersOutOfMine() {
        Location pos1 = mine.getPos1();
        Location pos2 = mine.getPos2();
        World world = pos1.getWorld();

        if (world == null) return;

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        List<Player> playersToTeleport = new ArrayList<>();

        for (Player player : world.getPlayers()) {
            Location playerLoc = player.getLocation();

            if (isLocationInRegion(playerLoc, minX, maxX, minY, maxY, minZ, maxZ)) {
                playersToTeleport.add(player);
            }
        }

        if (!playersToTeleport.isEmpty()) {
            Location safeLocation = mine.getSafeTeleportLocation();

            for (Player player : playersToTeleport) {
                teleportPlayerSafely(player, safeLocation);

                // Send simple notification
                plugin.getMessageUtil().sendMessage(player, "teleported-from-reset",
                        Map.of("mine", mine.getName()));
            }

            plugin.getLogger().info("Teleported " + playersToTeleport.size() +
                    " player(s) out of mine '" + mine.getName() + "'");
        }
    }

    private boolean isLocationInRegion(Location loc, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        return x >= minX && x <= maxX &&
                y >= minY && y <= maxY &&
                z >= minZ && z <= maxZ;
    }

    private void teleportPlayerSafely(Player player, Location location) {
        try {
            player.teleportAsync(location);
        } catch (NoSuchMethodError e) {
            player.teleport(location);
        }

        // Play teleport sound
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }

    private void resetMineBlocks() {
        Location pos1 = mine.getPos1();
        Location pos2 = mine.getPos2();

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        Map<Material, Double> composition = mine.getComposition();
        List<Material> materials = generateMaterialList(composition);
        Random random = new Random();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = pos1.getWorld().getBlockAt(x, y, z);
                    Material material = materials.get(random.nextInt(materials.size()));
                    block.setType(material, false);
                }
            }
        }
    }

    private List<Material> generateMaterialList(Map<Material, Double> composition) {
        List<Material> materials = new ArrayList<>();

        for (Map.Entry<Material, Double> entry : composition.entrySet()) {
            int count = (int) Math.round(entry.getValue());
            for (int i = 0; i < count; i++) {
                materials.add(entry.getKey());
            }
        }

        if (materials.isEmpty()) {
            materials.add(Material.STONE);
        }

        return materials;
    }
}
