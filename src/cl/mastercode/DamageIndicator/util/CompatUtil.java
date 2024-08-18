/*
 * Copyright 2018 YitanTribal & Beelzebu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cl.mastercode.DamageIndicator.util;

import cl.mastercode.DamageIndicator.DIMain;
import net.kyori.adventure.platform.bukkit.BukkitComponentSerializer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import java.util.Objects;

/**
 * Class to manage compatibility with older minecraft versions.
 *
 * @author Beelzebu
 */
public final class CompatUtil {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer legacySerializer = BukkitComponentSerializer.legacy();
    public static ItemStack RED_INK = null;
    public static int MINOR_VERSION = 8;

    public static void onEnable() {
        MINOR_VERSION = _getMinorVersion();
        if (MINOR_VERSION >= 13) {
            if (MINOR_VERSION == 13) {
                RED_INK = new ItemStack(Material.valueOf("ROSE_RED"));
            } else {
                RED_INK = new ItemStack(Material.RED_DYE);
            }
        } else {
            RED_INK = new ItemStack(Material.valueOf("INK_SACK"), 1, (short) 1);
        }
    }

    public static boolean isCanSetCollidable() {
        return MINOR_VERSION > 8;
    }

    public static boolean is113orHigher() {
        return _getMinorVersion() > 13;
    }

    public static double getMaxHealth(LivingEntity livingEntity) {
        if (MINOR_VERSION > 8) {
            return Objects.requireNonNull(livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();
        }
        return livingEntity.getMaxHealth();
    }

    private static int _getMinorVersion() {
        String ver = Bukkit.getServer().getBukkitVersion();
        int verInt = -1;
        try {
            verInt = Integer.parseInt(ver.split("\\.")[1]);
        } catch (IllegalArgumentException e) {
            Bukkit.getScheduler().runTask(DIMain.getPlugin(DIMain.class), () -> {
                DIMain.getPlugin(DIMain.class).getLogger().warning("An error occurred getting server version, please contact developer.");
                DIMain.getPlugin(DIMain.class).getLogger().warning("Detected version " + ver);
                Bukkit.getPluginManager().disablePlugin(DIMain.getPlugin(DIMain.class));
            });
        }
        return verInt;
    }

    public static ArmorStand buildArmorStand(Location location, double distance, FixedMetadataValue fixedMetadataValue, String name) {
        ArmorStand armorStand;
        if (MINOR_VERSION >= 11) {
            armorStand = modernEntitySpawn(location, distance, fixedMetadataValue);
        } else {
            armorStand = legacyEntitySpawn(location, distance, fixedMetadataValue);
        }
        armorStand.setCustomName(legacySerializer.serialize(miniMessage.deserialize(name)));
        armorStand.setCustomNameVisible(true);
        return armorStand;
    }

    private static ArmorStand modernEntitySpawn(Location location, double distance, FixedMetadataValue fixedMetadataValue) {
        return location.getWorld().spawn(location.clone().add(0, location.getWorld().getMaxHeight() - location.getY(), 0), ArmorStand.class, stand -> setStandProperties(stand, location, distance, fixedMetadataValue));
    }

    private static ArmorStand legacyEntitySpawn(Location location, double distance, FixedMetadataValue fixedMetadataValue) {
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        setStandProperties(armorStand, location, distance, fixedMetadataValue);
        return armorStand;
    }

    private static void setStandProperties(ArmorStand armorStand, Location location, double distance, FixedMetadataValue fixedMetadataValue) {
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setMarker(true);
        armorStand.setSmall(true);
        armorStand.setCustomNameVisible(false);
        armorStand.setMetadata("Mastercode-DamageIndicator", fixedMetadataValue);
        if (CompatUtil.isCanSetCollidable()) {
            armorStand.setCollidable(false);
            armorStand.setInvulnerable(true);
        }
        armorStand.teleport(location.clone().add(0, distance, 0));
        armorStand.setRemoveWhenFarAway(true);
    }
}
