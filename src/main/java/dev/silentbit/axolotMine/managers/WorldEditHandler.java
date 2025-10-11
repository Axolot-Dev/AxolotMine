package dev.silentbit.axolotMine.managers;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import dev.silentbit.axolotMine.AxolotMine;
import org.bukkit.entity.Player;

public class WorldEditHandler {

    private final AxolotMine plugin;

    public WorldEditHandler(AxolotMine plugin) {
        this.plugin = plugin;
    }

    public Region getSelection(Player player) throws IncompleteRegionException {
        com.sk89q.worldedit.bukkit.WorldEditPlugin worldEdit = getWorldEdit();
        return worldEdit.getSession(player).getSelection(BukkitAdapter.adapt(player.getWorld()));
    }

    private com.sk89q.worldedit.bukkit.WorldEditPlugin getWorldEdit() {
        return (com.sk89q.worldedit.bukkit.WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin("WorldEdit");
    }
}
