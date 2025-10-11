// src/main/java/dev/silentbit/axolotMine/utils/MessageUtil.java
package dev.silentbit.axolotMine.utils;

import dev.silentbit.axolotMine.AxolotMine;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class MessageUtil {

    private final AxolotMine plugin;
    private final Map<String, String> defaultMessages;

    public MessageUtil(AxolotMine plugin) {
        this.plugin = plugin;
        this.defaultMessages = new HashMap<>();
        loadDefaultMessages();
    }

    private void loadDefaultMessages() {
        // Hardcoded fallback messages (used if not in messages.yml)
        defaultMessages.put("no-permission", "<red>You don't have permission!</red>");
        defaultMessages.put("player-only", "<red>This command is for players only!</red>");
        defaultMessages.put("mine-not-found", "<red>Mine '<mine>' not found!</red>");
        defaultMessages.put("mine-already-exists", "<red>Mine '<mine>' already exists!</red>");
        defaultMessages.put("no-selection", "<red>Make a WorldEdit selection first!</red>");
        defaultMessages.put("unknown-command", "<red>Unknown command! Use /am help</red>");
        defaultMessages.put("no-mines", "<yellow>No mines created yet.</yellow>");
        defaultMessages.put("invalid-number", "<red>Invalid number!</red>");
        defaultMessages.put("invalid-percentage", "<red>Percentage must be 0-100!</red>");
        defaultMessages.put("invalid-material", "<red>Invalid material type!</red>");
        defaultMessages.put("interval-too-small", "<red>Interval must be at least 30 seconds!</red>");

        // Usage messages
        defaultMessages.put("usage-create", "<yellow>Usage: /am create <name></yellow>");
        defaultMessages.put("usage-reset", "<yellow>Usage: /am reset <name></yellow>");
        defaultMessages.put("usage-delete", "<yellow>Usage: /am delete <name></yellow>");
        defaultMessages.put("usage-setinterval", "<yellow>Usage: /am setinterval <name> <seconds></yellow>");
        defaultMessages.put("usage-settp", "<yellow>Usage: /am settp <name></yellow>");
        defaultMessages.put("usage-teleport", "<yellow>Usage: /am tp <name></yellow>");
        defaultMessages.put("usage-composition", "<yellow>Usage: /am comp <name> [add|remove] [material] [%]</yellow>");
        defaultMessages.put("usage-composition-add", "<yellow>Usage: /am comp <name> add <MATERIAL> <%></yellow>");
        defaultMessages.put("usage-composition-remove", "<yellow>Usage: /am comp <name> remove <MATERIAL></yellow>");

        // Success messages
        defaultMessages.put("config-reloaded", "<green>✓ Configuration reloaded!</green>");
        defaultMessages.put("all-mines-reset", "<green>✓ All <count> mines reset!</green>");
        defaultMessages.put("teleported-to-mine", "<aqua>Teleported to '<mine>'!</aqua>");
        defaultMessages.put("interval-set", "<green>✓ Interval set to <interval>s for '<mine>'!</green>");
        defaultMessages.put("composition-added", "<green>✓ Added <material> (<percentage>) to '<mine>'!</green>");
        defaultMessages.put("composition-removed", "<red>✗ Removed <material> from '<mine>'!</red>");
        defaultMessages.put("material-not-in-composition", "<red>Material not in composition!</red>");

        // List & Info headers
        defaultMessages.put("mines-list-header", "<gradient:#00ffaa:#00aaff>╔═════════════ Mines ═════════════╗</gradient>");
        defaultMessages.put("mines-list-entry", "<gradient:#00ffaa:#00aaff>║</gradient> <aqua><mine></aqua> <dark_gray>│</dark_gray> <gray><world> <dark_gray>│</dark_gray> <white><size></white> <dark_gray>│</dark_gray> <yellow><blocks></yellow> blocks <dark_gray>│</dark_gray> Next: <green><time></green></gray>");

        defaultMessages.put("mine-info-header", "<gradient:#00ffaa:#00aaff>╔════════ <mine> ════════╗</gradient>");
        defaultMessages.put("mine-info-world", "<gradient:#00ffaa:#00aaff>║</gradient> World: <white><world></white>");
        defaultMessages.put("mine-info-size", "<gradient:#00ffaa:#00aaff>║</gradient> Size: <white><size></white>");
        defaultMessages.put("mine-info-blocks", "<gradient:#00ffaa:#00aaff>║</gradient> Blocks: <yellow><blocks></yellow>");
        defaultMessages.put("mine-info-interval", "<gradient:#00ffaa:#00aaff>║</gradient> Interval: <gold><interval>s</gold>");
        defaultMessages.put("mine-info-nextreset", "<gradient:#00ffaa:#00aaff>║</gradient> Next Reset: <green><time></green>");
        defaultMessages.put("mine-info-spawnpoint", "<gradient:#00ffaa:#00aaff>║</gradient> Spawn: <aqua>X:<x> Y:<y> Z:<z></aqua>");
        defaultMessages.put("mine-info-no-spawnpoint", "<gradient:#00ffaa:#00aaff>║</gradient> <yellow>No spawn point set! Use /am settp</yellow>");
        defaultMessages.put("mine-info-composition", "<gradient:#00ffaa:#00aaff>║</gradient> <yellow>Composition:</yellow>");
        defaultMessages.put("mine-info-composition-entry", "<gradient:#00ffaa:#00aaff>║</gradient>   <aqua>• <material>:</aqua> <gold><percentage></gold>");

        defaultMessages.put("composition-header", "<gradient:#00ffaa:#00aaff>╔═════ <mine> Composition ═════╗</gradient>");
        defaultMessages.put("composition-entry", "<gradient:#00ffaa:#00aaff>║</gradient> <aqua>• <material></aqua> <dark_gray>→</dark_gray> <gold><percentage></gold>");

        defaultMessages.put("plugin-info-header", "<gradient:#00ffaa:#00aaff>╔══════════ AxolotMine ══════════╗</gradient>");
        defaultMessages.put("plugin-info-version", "<gradient:#00ffaa:#00aaff>║</gradient> Version: <yellow><version></yellow>");
        defaultMessages.put("plugin-info-author", "<gradient:#00ffaa:#00aaff>║</gradient> Author: <aqua>SilentBit Development Team</aqua>");
        defaultMessages.put("plugin-info-mines", "<gradient:#00ffaa:#00aaff>║</gradient> Total Mines: <green><count></green>");

        // Help messages
        defaultMessages.put("help-header", "<gradient:#00ffaa:#00aaff>╔════════ AxolotMine Commands ════════╗</gradient>");
        defaultMessages.put("help-create", "<gradient:#00ffaa:#00aaff>║</gradient> <yellow>/am create <name></yellow> <dark_gray>→</dark_gray> <gray>Create mine</gray>");
        defaultMessages.put("help-reset", "<gradient:#00ffaa:#00aaff>║</gradient> <yellow>/am reset <name></yellow> <dark_gray>→</dark_gray> <gray>Reset mine</gray>");
        defaultMessages.put("help-resetall", "<gradient:#00ffaa:#00aaff>║</gradient> <yellow>/am resetall</yellow> <dark_gray>→</dark_gray> <gray>Reset all</gray>");
        defaultMessages.put("help-delete", "<gradient:#00ffaa:#00aaff>║</gradient> <yellow>/am delete <name></yellow> <dark_gray>→</dark_gray> <gray>Delete mine</gray>");
        defaultMessages.put("help-list", "<gradient:#00ffaa:#00aaff>║</gradient> <yellow>/am list</yellow> <dark_gray>→</dark_gray> <gray>List mines</gray>");
        defaultMessages.put("help-info", "<gradient:#00ffaa:#00aaff>║</gradient> <yellow>/am info [mine]</yellow> <dark_gray>→</dark_gray> <gray>Show info</gray>");
        defaultMessages.put("help-setinterval", "<gradient:#00ffaa:#00aaff>║</gradient> <yellow>/am setinterval <name> <sec></yellow> <dark_gray>→</dark_gray> <gray>Set interval</gray>");
        defaultMessages.put("help-settp", "<gradient:#00ffaa:#00aaff>║</gradient> <yellow>/am settp <name></yellow> <dark_gray>→</dark_gray> <gray>Set spawn point</gray>");
        defaultMessages.put("help-teleport", "<gradient:#00ffaa:#00aaff>║</gradient> <yellow>/am tp <name></yellow> <dark_gray>→</dark_gray> <gray>Teleport to mine</gray>");
        defaultMessages.put("help-composition", "<gradient:#00ffaa:#00aaff>║</gradient> <yellow>/am comp <name> [add|remove]</yellow> <dark_gray>→</dark_gray> <gray>Edit blocks</gray>");
        defaultMessages.put("help-reload", "<gradient:#00ffaa:#00aaff>║</gradient> <yellow>/am reload</yellow> <dark_gray>→</dark_gray> <gray>Reload config</gray>");
        defaultMessages.put("help-footer", "<gradient:#00ffaa:#00aaff>╚═════════════════════════════════════╝</gradient>");
    }

    public void sendMessage(CommandSender sender, String key) {
        sendMessage(sender, key, Map.of());
    }

    public void sendMessage(CommandSender sender, String key, Map<String, String> placeholders) {
        // Try to get from messages.yml first
        String message = plugin.getConfigManager().getMessages().getString(key);

        // Fallback to hardcoded default if not found
        if (message == null || message.isEmpty()) {
            message = defaultMessages.getOrDefault(key, "<red>Message '" + key + "' not found!</red>");
        }

        // Replace placeholders
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("<" + entry.getKey() + ">", entry.getValue());
        }

        // Add prefix if not a header/footer/list entry
        if (!key.contains("header") && !key.contains("footer") && !key.contains("entry")
                && !key.contains("info-") && !key.contains("help-")) {
            String prefix = plugin.getConfigManager().getMessages().getString("prefix", "");
            if (!prefix.isEmpty()) {
                message = prefix + message;
            }
        }

        Component component = plugin.getMiniMessage().deserialize(message);
        sender.sendMessage(component);
    }
}
