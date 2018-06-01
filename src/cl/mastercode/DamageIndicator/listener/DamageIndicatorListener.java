/**
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
package cl.mastercode.DamageIndicator.listener;

import cl.mastercode.DamageIndicator.DIMain;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.metadata.FixedMetadataValue;

/**
 *
 * @author YitanTribal, Beelzebu
 */
public class DamageIndicatorListener implements Listener {

    private final DIMain plugin;
    @Getter
    private final LinkedHashMap<ArmorStand, Long> armorStands = new LinkedHashMap<>();
    private final boolean enablePlayer, enableMonster, enableAnimal;

    public DamageIndicatorListener(DIMain plugin) {
        this.plugin = plugin;
        enablePlayer = plugin.getConfig().getBoolean("Damage Indicator.Player");
        enableMonster = plugin.getConfig().getBoolean("Damage Indicator.Monster");
        enableAnimal = plugin.getConfig().getBoolean("Damage Indicator.Animals");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity.getType().equals(EntityType.ARMOR_STAND)) {
                ArmorStand as = (ArmorStand) entity;
                if (plugin.isDamageIndicator(as)) {
                    armorStands.remove(as);
                    as.remove();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkload(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity.getType().equals(EntityType.ARMOR_STAND)) {
                ArmorStand as = (ArmorStand) entity;
                if (plugin.isDamageIndicator(as)) {
                    armorStands.remove(as);
                    as.remove();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityRegainHealth(EntityRegainHealthEvent e) {
        if (!e.isCancelled()) {
            handleArmorStand(e.getEntity(), ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Damage Indicator.Format.EntityRegain").replace("%health%", damageFormat(e.getAmount()))), e.getEntity().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageEvent(EntityDamageEvent e) {
        if (!e.isCancelled()) {
            handleArmorStand(e.getEntity(), ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Damage Indicator.Format.EntityDamage").replace("%damage%", damageFormat(e.getFinalDamage()))), e.getEntity().getLocation());
        }
    }

    private String damageFormat(Object o) {
        DecimalFormat df;
        try {
            df = new DecimalFormat(plugin.getConfig().getString("Damage Indicator.Format.Decimal", "#.##"));
        } catch (Exception ex) {
            df = new DecimalFormat("#.##");
        }
        return df.format(o);
    }

    private void handleArmorStand(Entity entity, String format, Location loc) {
        if (entity.hasMetadata("NPC")) {
            return;
        }
        if (entity instanceof ArmorStand) {
            return;
        }
        if (entity instanceof Player && !enablePlayer) {
            return;
        }
        if ((entity instanceof Monster || entity instanceof Slime || entity instanceof MagmaCube) && !enableMonster) {
            return;
        }
        if (entity instanceof Animals && !enableAnimal) {
            return;
        }
        armorStands.put(getDefaultArmorStand(loc, format), System.currentTimeMillis());
    }

    public ArmorStand getDefaultArmorStand(Location loc, String name) {
        Location spawnLoc = new Location(loc.getWorld(), loc.getX(), 500, loc.getZ());
        ArmorStand as = (ArmorStand) loc.getWorld().spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
        as.setVisible(false);
        as.setCustomNameVisible(false);
        as.setSmall(true);
        as.setRemoveWhenFarAway(true);
        as.setMetadata("Mastercode-DamageIndicator", new FixedMetadataValue(plugin, 1));
        as.setGravity(false);
        try {
            as.setCollidable(false);
            as.setInvulnerable(true);
        } catch (Exception oldVersion) {
        }
        as.setMarker(true);
        as.teleport(loc.add(0.0, 1.6, 0.0));
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            as.setCustomNameVisible(true);
        }, 5);
        as.setCustomName(name);
        return as;
    }
}