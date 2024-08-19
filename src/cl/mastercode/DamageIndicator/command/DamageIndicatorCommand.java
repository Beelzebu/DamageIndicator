package cl.mastercode.DamageIndicator.command;

import cl.mastercode.DamageIndicator.DIMain;
import static cl.mastercode.DamageIndicator.util.CompatUtil.LEGACY_SERIALIZER;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.nifheim.bukkit.commandlib.RegistrableCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class DamageIndicatorCommand extends RegistrableCommand {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final DIMain plugin;

    public DamageIndicatorCommand(DIMain plugin) {
        super(plugin, "damageindicator", "damageindicator.use", false, "di", "dindicator");
        this.plugin = plugin;
    }

    @Override
    public void onCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(sender);
        } else if (args[0].equalsIgnoreCase("toggle")) {
            if (sender instanceof Player player) {
                boolean status = !plugin.getStorageProvider().showArmorStand(player);
                plugin.getStorageProvider().setShowArmorStand(player, status);
                if (status) {
                    sendMessage(sender, "Command.Damage Indicator.Enabled");
                } else {
                    sendMessage(sender, "Command.Damage Indicator.Disabled");
                }
            } else {
                sendMessage(sender, "Command.No Console");
            }
        } else if (sender.hasPermission("damageindicator.use.admin")) {
            switch (args[0].toLowerCase()) {
                case "reload":
                    plugin.reload();
                    sendMessage(sender, "Command.Reload");
                    break;
                case "clear":
                    if (sender instanceof Player player) {
                        if (args.length == 2) {
                            int range = getInt(sender, args[1]);
                            if (range > 0) {
                                long total = player.getNearbyEntities(range, range, range).stream().filter(entity -> entity instanceof ArmorStand && plugin.isDamageIndicator(entity)).peek(Entity::remove).count();
                                sendMessage(sender, "Command.Clear.Total", String.valueOf(total), String.valueOf(range));
                            }
                        } else {
                            sendHelpMessage(sender);
                        }
                    } else {
                        sendMessage(sender, "Command.No Console");
                    }
                    break;
                case "clearall":
                    if (sender instanceof Player player) {
                        long total = player.getWorld().getEntitiesByClass(ArmorStand.class).stream().filter(entity -> entity != null && plugin.isDamageIndicator(entity)).peek(Entity::remove).count();
                        sendMessage(sender, "Command.Clear.All", String.valueOf(total));
                    } else {
                        sendMessage(sender, "Command.No Console");
                    }
                    break;
            }
        }
    }

    private void sendMessage(CommandSender sender, String path, String... replacements) {
        String message = plugin.getMessages().getString(path);
        if (message == null || message.isBlank()) return;
        for (int i = 0; i < replacements.length; i++) {
            message = message.replace("{" + i + "}", replacements[i]);
        }
        sender.sendMessage(LEGACY_SERIALIZER.serialize(miniMessage.deserialize(message)));
    }

    private void sendHelpMessage(CommandSender sender) {
        sendMessage(sender, "Command.Help.Header");
        if (sender.hasPermission("damageindicator.use.admin")) {
            sendMessage(sender, "Command.Help.Reload");
            sendMessage(sender, "Command.Help.Clear");
            sendMessage(sender, "Command.Help.Clear All");
        }
        sendMessage(sender, "Command.Help.Toggle");
    }

    private int getInt(CommandSender sender, String text) {
        int amount = 0;
        try {
            amount = Integer.parseInt(text);
        } catch (NumberFormatException ignored) {
        }
        if (amount > 0) return amount;
        sendMessage(sender, "Command.Clear.Invalid", text);
        return amount;
    }
}
