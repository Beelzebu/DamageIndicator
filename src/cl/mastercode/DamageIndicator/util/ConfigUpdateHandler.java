package cl.mastercode.DamageIndicator.util;

import cl.mastercode.DamageIndicator.DIMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ConfigUpdateHandler {

    private final DIMain plugin;
    private final Path configFile;
    private final Path messagesFile;
    private final String lineSeparator = System.getProperty("line.separator");

    public ConfigUpdateHandler(DIMain plugin) {
        this.plugin = plugin;
        configFile = plugin.getDataFolder().toPath().resolve("config.yml");
        messagesFile = plugin.getDataFolder().toPath().resolve("messages.yml");
    }

    public void updateConfig() {
        if (!configFile.toFile().exists()) {
            plugin.saveResource("config.yml", false);
        }
        plugin.reloadConfig();
        if (plugin.getConfig().getInt("version", 1) < 2) {
            updateToV2();
        }
        if (plugin.getConfig().getInt("version", 2) < 3) {
            updateToV3();
        }
        if (plugin.getConfig().getInt("version", 3) < 4) {
            updateToV4();
        }
        if (plugin.getConfig().getInt("version", 4) < 5) {
            updateToV5();
        }
    }

    private void updateToV2() {
        String[] lines = new String[]{
                "# DamageIndicator Reborn, Minecraft plugin to show the damage taken by a entity",
                "# Source Code: https://github.com/Beelzebu/DamageIndicator",
                "# Issue Tracker: https://github.com/Beelzebu/DamageIndicator/issues",
                "",
                "# Config version, don't edit",
                "version: 2",
                "",
                "# Damage Indicator options, here you can define in what type of entities we",
                "# should show the damage indicators and completly disable this feature.",
                "Damage Indicator:",
                "  Enabled: true",
                "  Player: " + plugin.getConfig().getBoolean("UseAt.Player", true),
                "  Monster: " + plugin.getConfig().getBoolean("UseAt.Monster", true),
                "  Animals: " + plugin.getConfig().getBoolean("UseAt.Animals", true),
                "  # Use %health% for the regain health you get",
                "  # Use %damage% for the damage you get",
                "  Format:",
                "    EntityRegain: '" + plugin.getConfig().getString("Format.EntityRegain", "&7+&a%health%") + "'",
                "    EntityDamage: '" + plugin.getConfig().getString("Format.EntityDamage", "&7-&c%damage%") + "'",
                "    # Here you define the decimal format for the damage and health",
                "    # See https://docs.oracle.com/javase/8/docs/api/java/text/DecimalFormat.html",
                "    # for more information.",
                "    Decimal: '" + plugin.getConfig().getString("Format.Decimal", "#.##") + "'",
                "",
                "# Blood here you can completly disable this feature.",
                "Blood:",
                "  Enabled: true"
        };
        try {
            Files.write(configFile, List.of(lines));
            plugin.reloadConfig();
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&eDamageIndicator config updated to v2."));
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "Can't save config v2", ex);
        }
    }

    private void updateToV3() {
        String[] lines = new String[]{
                "# DamageIndicator Reborn, Minecraft plugin to show the damage taken by a entity",
                "# Source Code: https://github.com/Beelzebu/DamageIndicator",
                "# Issue Tracker: https://github.com/Beelzebu/DamageIndicator/issues",
                "",
                "# Config version, don't edit",
                "version: 3",
                "",
                "# Damage Indicator options, here you can define in what type of entities we",
                "# should show the damage indicators and completely disable this feature.",
                "Damage Indicator:",
                "  Enabled: " + plugin.getConfig().getBoolean("Damage Indicator.Enabled"),
                "  Player: " + plugin.getConfig().getBoolean("Damage Indicator.Player"),
                "  Monster: " + plugin.getConfig().getBoolean("Damage Indicator.Monster"),
                "  Animals: " + plugin.getConfig().getBoolean("Damage Indicator.Animals"),
                "  # Use %health% for the regain health you get",
                "  # Use %damage% for the damage you get",
                "  Format:",
                "    EntityRegain: '" + plugin.getConfig().getString("Damage Indicator.Format.EntityRegain") + "'",
                "    EntityDamage: '" + plugin.getConfig().getString("Damage Indicator.Format.EntityDamage") + "'",
                "    # Here you define the decimal format for the damage and health",
                "    # See https://docs.oracle.com/javase/8/docs/api/java/text/DecimalFormat.html",
                "    # for more information.",
                "    Decimal: '" + plugin.getConfig().getString("Damage Indicator.Format.Decimal") + "'",
                "  # Distance in Y axis from the entity where we should spawn the damage indicator",
                "  # A distance of 2 is recommended for 1.8 servers",
                "  Distance: 1.6",
                "  # List of disabled entity types that shouldn't spawn damage indicators",
                "  # See https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html",
                "  # for all possible entities.",
                "  Disabled Entities:",
                "  - GHAST",
                "  - ZOMBIE",
                "  - IRON_GOLEM",
                "  # List of spawn reasons that we must listen and mark entities so they won't spawn",
                "  # damage indicators.",
                "  # See https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/entity/CreatureSpawnEvent.SpawnReason.html",
                "  # for all possible spawn reasons.",
                "  Disabled Reasons:",
                "  - SPAWNER",
                "",
                "# Blood options, here you can configure this feature.",
                "Blood:",
                "  Enabled: " + plugin.getConfig().getBoolean("Blood.Enabled"),
                "  Player: true",
                "  Monster: true",
                "  Animals: true",
                "  # List of disabled entity types that shouldn't bleed.",
                "  # See https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html",
                "  # for all possible entities.",
                "  Disabled Entities:",
                "  - ZOMBIE",
                "  - SKELETON",
                "  # List of spawn reasons that we must listen and mark entities so they won't bleed.",
                "  # See https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/entity/CreatureSpawnEvent.SpawnReason.html",
                "  # for all possible spawn reasons.",
                "  Disabled Reasons:",
                "  - SPAWNER",
                "",
                "Messages:",
                "  Damage Indicator:",
                "    Enabled: '" + plugin.getConfig().getString("Messages.Damage Indicator.Enabled") + "'",
                "    Disabled: '" + plugin.getConfig().getString("Messages.Damage Indicator.Disabled") + "'"
        };
        try {
            Files.write(configFile, List.of(lines));
            plugin.reloadConfig();
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&eDamageIndicator config updated to v3."));
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "Can't save config v3", ex);
        }
    }

    private void updateToV4() {
        String[] lines = new String[]{
                "# DamageIndicator Reborn, Minecraft plugin to show the damage taken by a entity",
                "# Source Code: https://github.com/Beelzebu/DamageIndicator",
                "# Issue Tracker: https://github.com/Beelzebu/DamageIndicator/issues",
                "",
                "# Config version, don't edit",
                "version: 4",
                "",
                "# Damage Indicator options, here you can define in what type of entities we",
                "# should show the damage indicators and completely disable this feature.",
                "Damage Indicator:",
                "  Enabled: " + plugin.getConfig().getBoolean("Damage Indicator.Enabled"),
                "  Player: " + plugin.getConfig().getBoolean("Damage Indicator.Player"),
                "  Monster: " + plugin.getConfig().getBoolean("Damage Indicator.Monster"),
                "  Animals: " + plugin.getConfig().getBoolean("Damage Indicator.Animals"),
                "  # Use %health% for the regain health you get",
                "  # Use %damage% for the damage you get",
                "  Format:",
                "    EntityRegain: '" + plugin.getConfig().getString("Damage Indicator.Format.EntityRegain") + "'",
                "    EntityDamage: '" + plugin.getConfig().getString("Damage Indicator.Format.EntityDamage") + "'",
                "    # Here you define the decimal format for the damage and health",
                "    # See https://docs.oracle.com/javase/8/docs/api/java/text/DecimalFormat.html",
                "    # for more information.",
                "    Decimal: '" + plugin.getConfig().getString("Damage Indicator.Format.Decimal") + "'",
                "  # Distance in Y axis from the entity where we should spawn the damage indicator",
                "  # A distance of 2 is recommended for 1.8 servers",
                "  Distance: " + plugin.getConfig().getDouble("Damage Indicator.Distance"),
                "  # List of disabled entity types that shouldn't spawn damage indicators",
                "  # See https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html",
                "  # for all possible entities.",
                "  Disabled Entities:" + (plugin.getConfig().getStringList("Damage Indicator.Disabled Entities").isEmpty() ? " []" : plugin.getConfig().getStringList("Damage Indicator.Disabled Entities").stream().map(line -> lineSeparator + "  - " + line).collect(Collectors.joining())),
                "  # List of spawn reasons that we must listen and mark entities so they won't spawn",
                "  # damage indicators.",
                "  # See https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/entity/CreatureSpawnEvent.SpawnReason.html",
                "  # for all possible spawn reasons.",
                "  Disabled Spawn Reasons:" + (plugin.getConfig().getStringList("Damage Indicator.Disabled Reasons").isEmpty() ? " []" : plugin.getConfig().getStringList("Damage Indicator.Disabled Reasons").stream().map(line -> lineSeparator + "  - " + line).collect(Collectors.joining())),
                "  # List of damage causes that we must ignore for damage indicators.",
                "  # See https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/EntityDamageEvent.DamageCause.html",
                "  # for all possible damage causes.",
                "  Disabled Damage Causes:",
                "  - SUICIDE",
                "  # Enable or disable damage indicators for players who are sneaking, it is recommended to disable for PvP servers.",
                "  # true: damage indicators are shown even if you're sneaking.",
                "  # false: damage indicators aren't shown when you're sneaking.",
                "  Sneaking: false",
                "# Blood options, here you can configure this feature.",
                "Blood:",
                "  Enabled: " + plugin.getConfig().getBoolean("Blood.Enabled"),
                "  Player: true" + plugin.getConfig().getBoolean("Blood.Player"),
                "  Monster: true" + plugin.getConfig().getBoolean("Blood.Monster"),
                "  Animals: true" + plugin.getConfig().getBoolean("Blood.Animals"),
                "  # List of disabled entity types that shouldn't bleed.",
                "  # See https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html",
                "  # for all possible entities.",
                "  Disabled Entities:" + (plugin.getConfig().getStringList("Blood.Disabled Entities").isEmpty() ? " []" : plugin.getConfig().getStringList("Blood.Disabled Entities").stream().map(line -> lineSeparator + "  - " + line).collect(Collectors.joining())),
                "  # List of spawn reasons that we must listen and mark entities so they won't bleed.",
                "  # See https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/entity/CreatureSpawnEvent.SpawnReason.html",
                "  # for all possible spawn reasons.",
                "  Disabled Spawn Reasons:" + (plugin.getConfig().getStringList("Blood.Disabled Reasons").isEmpty() ? " []" : plugin.getConfig().getStringList("Blood.Disabled Reasons").stream().map(line -> lineSeparator + "  - " + line).collect(Collectors.joining())),
                "  # List of damage causes that we must ignore for blood.",
                "  # See https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/EntityDamageEvent.DamageCause.html",
                "  # for all possible damage causes.",
                "  Disabled Damage Causes:",
                "  - DROWNING",
                "  # Enable or disable blood particles for players who are sneaking.",
                "  # true: blood particles are shown even if you're sneaking.",
                "  # false: blood particles aren't shown when you're sneaking.",
                "  Sneaking: true",
                "",
                "Messages:",
                "  Damage Indicator:",
                "    Enabled: '" + plugin.getConfig().getString("Messages.Damage Indicator.Enabled") + "'",
                "    Disabled: '" + plugin.getConfig().getString("Messages.Damage Indicator.Disabled") + "'"
        };
        try {
            Files.write(configFile, List.of(lines));
            plugin.reloadConfig();
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&eDamageIndicator config updated to v4."));
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "Can't save config v4", ex);
        }
    }

    private void updateToV5() {
        String[] lines = new String[]{
                "# DamageIndicator Reborn, Minecraft plugin to show the damage taken by a entity",
                "# Source Code: https://github.com/Beelzebu/DamageIndicator",
                "# Issue Tracker: https://github.com/Beelzebu/DamageIndicator/issues",
                "",
                "# Config version, don't edit",
                "version: 5",
                "",
                "# Damage Indicator options, here you can define in what type of entities we",
                "# should show the damage indicators and completely disable this feature.",
                "Damage Indicator:",
                "  Enabled: " + plugin.getConfig().getBoolean("Damage Indicator.Enabled"),
                "  Player: " + plugin.getConfig().getBoolean("Damage Indicator.Player"),
                "  Monster: " + plugin.getConfig().getBoolean("Damage Indicator.Monster"),
                "  Animals: " + plugin.getConfig().getBoolean("Damage Indicator.Animals"),
                "  # Use %health% for the regain health you get",
                "  # Use %damage% for the damage you get",
                "  Format:",
                "    EntityRegain: '" + plugin.getConfig().getString("Damage Indicator.Format.EntityRegain") + "'",
                "    EntityDamage: '" + plugin.getConfig().getString("Damage Indicator.Format.EntityDamage") + "'",
                "    # Here you define the decimal format for the damage and health",
                "    # See https://docs.oracle.com/javase/8/docs/api/java/text/DecimalFormat.html",
                "    # for more information.",
                "    Decimal: '" + plugin.getConfig().getString("Damage Indicator.Format.Decimal") + "'",
                "  # Distance in Y axis from the entity where we should spawn the damage indicator",
                "  # A distance of 2 is recommended for 1.8 servers",
                "  Distance: " + plugin.getConfig().getDouble("Damage Indicator.Distance"),
                "  # List of disabled entity types that shouldn't spawn damage indicators",
                "  # See https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html",
                "  # for all possible entities.",
                "  Disabled Entities:" + (plugin.getConfig().getStringList("Damage Indicator.Disabled Entities").isEmpty() ? " []" : plugin.getConfig().getStringList("Damage Indicator.Disabled Entities").stream().map(line -> lineSeparator + "  - " + line).collect(Collectors.joining())),
                "  # List of spawn reasons that we must listen and mark entities so they won't spawn",
                "  # damage indicators.",
                "  # See https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/entity/CreatureSpawnEvent.SpawnReason.html",
                "  # for all possible spawn reasons.",
                "  Disabled Spawn Reasons:" + (plugin.getConfig().getStringList("Damage Indicator.Disabled Reasons").isEmpty() ? " []" : plugin.getConfig().getStringList("Damage Indicator.Disabled Reasons").stream().map(line -> lineSeparator + "  - " + line).collect(Collectors.joining())),
                "  # List of damage causes that we must ignore for damage indicators.",
                "  # See https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/EntityDamageEvent.DamageCause.html",
                "  # for all possible damage causes.",
                "  Disabled Damage Causes:" + (plugin.getConfig().getStringList("Damage Indicator.Disabled Damage Causes").isEmpty() ? " []" : plugin.getConfig().getStringList("Damage Indicator.Disabled Damage Causes").stream().map(line -> lineSeparator + "  - " + line).collect(Collectors.joining())),
                "  # Enable or disable damage indicators for players who are sneaking, it is recommended to disable for PvP servers.",
                "  # true: damage indicators are shown even if you're sneaking.",
                "  # false: damage indicators aren't shown when you're sneaking.",
                "  Sneaking: false",
                "# Blood options, here you can configure this feature.",
                "Blood:",
                "  Enabled: " + plugin.getConfig().getBoolean("Blood.Enabled"),
                "  Player: true" + plugin.getConfig().getBoolean("Blood.Player"),
                "  Monster: true" + plugin.getConfig().getBoolean("Blood.Monster"),
                "  Animals: true" + plugin.getConfig().getBoolean("Blood.Animals"),
                "  # List of disabled entity types that shouldn't bleed.",
                "  # See https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html",
                "  # for all possible entities.",
                "  Disabled Entities:" + (plugin.getConfig().getStringList("Blood.Disabled Entities").isEmpty() ? " []" : plugin.getConfig().getStringList("Blood.Disabled Entities").stream().map(line -> lineSeparator + "  - " + line).collect(Collectors.joining())),
                "  # List of spawn reasons that we must listen and mark entities so they won't bleed.",
                "  # See https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/entity/CreatureSpawnEvent.SpawnReason.html",
                "  # for all possible spawn reasons.",
                "  Disabled Spawn Reasons:" + (plugin.getConfig().getStringList("Blood.Disabled Reasons").isEmpty() ? " []" : plugin.getConfig().getStringList("Blood.Disabled Reasons").stream().map(line -> lineSeparator + "  - " + line).collect(Collectors.joining())),
                "  # List of damage causes that we must ignore for blood.",
                "  # See https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/EntityDamageEvent.DamageCause.html",
                "  # for all possible damage causes.",
                "  Disabled Damage Causes:" + (plugin.getConfig().getStringList("Blood.Disables Damage Causes").isEmpty() ? " []" : plugin.getConfig().getStringList("Blood.Disabled Damage Causes").stream().map(line -> lineSeparator + "  - " + line).collect(Collectors.joining())),
                "  # Enable or disable blood particles for players who are sneaking.",
                "  # true: blood particles are shown even if you're sneaking.",
                "  # false: blood particles aren't shown when you're sneaking.",
                "  Sneaking: " + plugin.getConfig().getBoolean("Blood.Sneaking")
        };

        try {
            Files.write(configFile, List.of(lines));
            plugin.reloadConfig();
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&eDamageIndicator config updated to v5."));
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "Can't save config v5", ex);
        }

        String[] messagesLines = new String[]{
                "version: 1",
                "",
                "Command:",
                "  Help:",
                "    Header: \"<click:open_url:'https://www.spigotmc.org/resources/55974/'><color:#ff144b><st><b>━━━━━━━━━━━━━━━━</b></st></color> <b><color:#ff708a>Damage Indicator</color></b> <color:#ff144b><st><b>━━━━━━━━━━━━━━━━━━━━</b></st></color></click>\"",
                "    Reload: \"  <color:#fffaaa>/di reload</color> <b>-</b> Reload the plugin\"",
                "    Clear: \"  <color:#fffaaa>/di clear <range></color> <b>-</b> Clear damage indicators in range\"",
                "    Clear All: \"  <color:#fffaaa>/di clearall</color> <b>-</b> Clear all damage indicators\"",
                "    Toggle: \"  <color:#fffaaa>/di toggle</color> <b>-</b> Toggle damage indicators\"",
                "  Clear:",
                "    Total: \"<color:#00ff62>{0} Damage indicators were removed in a radius of {1}!</color>\"",
                "    All: \"<color:#00ff62>{0} Damage indicators were removed!</color>\"",
                "    Invalid: \"{0} <color:#ff0f23>is not a valid value</color>\"",
                "  Reload: \"<color:#00ff62>Plugin was reloaded successfully!</color>\"",
                "  No Console: \"<color:#ff0f23>This command can't be executed from console.</color>\"",
                "  Damage Indicator:",
                "    Enabled: \"<gray>You have enabled damage indicators</gray>\"",
                "    Disabled: \"<gray>You are no longer viewing damage indicators</gray>\""
        };

        try {
            Files.write(messagesFile, List.of(messagesLines));
            plugin.reloadMessages();
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&eDamageIndicator messages updated to v1."));
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "Can't save messages v1", ex);
        }
    }
}
