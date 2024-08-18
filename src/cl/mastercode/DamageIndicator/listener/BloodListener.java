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
package cl.mastercode.DamageIndicator.listener;

import cl.mastercode.DamageIndicator.DIMain;
import cl.mastercode.DamageIndicator.util.CompatUtil;
import cl.mastercode.DamageIndicator.util.ConfigUtil;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

/**
 * @author Beelzebu
 */
public class BloodListener implements Listener {

    private static final String BLOOD_NAME = "di-blood";
    private static final String DISABLED_BLOOD = "DI-DISABLED-BLOOD";
    private static final ItemStack[] BLOOD_ITEMS = new ItemStack[]{new ItemStack(Material.CRIMSON_ROOTS), new ItemStack(Material.FIRE_CORAL_FAN), new ItemStack(Material.RED_DYE)};
    private final DIMain plugin;
    private final Map<Item, Long> bloodItems = new LinkedHashMap<>();
    private final Set<EntityType> disabledEntities = new HashSet<>();
    private final Set<CreatureSpawnEvent.SpawnReason> disabledSpawnReasons = new HashSet<>();
    private final Set<EntityDamageEvent.DamageCause> disabledDamageCauses = new HashSet<>();
    private final Random random = new Random();
    private boolean enabled = true;
    private boolean enablePlayer = true;
    private boolean enableMonster = true;
    private boolean enableAnimal = true;
    private boolean sneaking = true;

    public BloodListener(DIMain plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        disabledEntities.clear();
        disabledSpawnReasons.clear();
        enabled = plugin.getConfig().getBoolean("Blood.Enabled");
        enablePlayer = plugin.getConfig().getBoolean("Blood.Player");
        enableMonster = plugin.getConfig().getBoolean("Blood.Monster");
        enableAnimal = plugin.getConfig().getBoolean("Blood.Animals");
        sneaking = plugin.getConfig().getBoolean("Blood.Sneaking");
        plugin.getConfig().getStringList("Blood.Disabled Entities").stream().map(entity -> {
            try {
                return EntityType.valueOf(entity.toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.WARNING, entity.toUpperCase() + " is not a valid EntityType.");
                return null;
            }
        }).filter(Objects::nonNull).forEach(disabledEntities::add);
        plugin.getConfig().getStringList("Blood.Disabled Spawn Reasons").stream().map(reason -> {
            try {
                return CreatureSpawnEvent.SpawnReason.valueOf(reason.toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.WARNING, reason.toUpperCase() + " is not a valid SpawnReason.");
                return null;
            }
        }).filter(Objects::nonNull).forEach(disabledSpawnReasons::add);
        plugin.getConfig().getStringList("Blood.Disabled Damage Causes").stream().map(cause -> {
            try {
                return EntityDamageEvent.DamageCause.valueOf(cause);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.WARNING, cause.toUpperCase() + " is not a valid DamageCause.");
                return null;
            }
        }).filter(Objects::nonNull).forEach(disabledDamageCauses::add);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (!showBlood(e.getEntity(), null, .1)) {
            return;
        }
        if (disabledSpawnReasons.contains(e.getSpawnReason())) {
            e.getEntity().setMetadata(DISABLED_BLOOD, new FixedMetadataValue(plugin, 1));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Entity entity = e.getEntity();
        if (!showBlood(entity, e.getCause(), e.getFinalDamage())) {
            return;
        }
        LivingEntity livingEntity = (LivingEntity) entity;
        e.getEntity().getWorld().spawnParticle(CompatUtil.BLOOD_PARTICLE, livingEntity.getEyeLocation(), 7, .5, .2, .5, 10, new Particle.DustOptions(Color.fromBGR(61, 61, 255), 3f));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemPickup(EntityPickupItemEvent e) {
        checkBloodItem(e.getItem(), e);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemPickup(PlayerPickupItemEvent e) {
        checkBloodItem(e.getItem(), e);
    }

    @EventHandler
    public void onInventoryPickup(InventoryPickupItemEvent e) {
        checkBloodItem(e.getItem(), e);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (!showBlood(e.getEntity(), e.getEntity().getLastDamageCause() != null ? e.getEntity().getLastDamageCause().getCause() : null, .1)) {
            return;
        }
        for (int i = 0; i < 3; i++) {
            e.getEntity().getWorld().playEffect(e.getEntity().getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
        }
        e.getEntity().getWorld().playEffect(e.getEntity().getLocation(), Effect.STEP_SOUND, Material.REDSTONE_WIRE, 2);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (!enablePlayer) {
            return;
        }
        for (int i = 0; i < 14; i++) {
            ItemStack is = BLOOD_ITEMS[i % 3];
            ItemMeta meta = is.getItemMeta();
            // check if meta was already set for item on the array
            if (!meta.hasDisplayName()) {
                meta.setDisplayName(BLOOD_NAME);
                meta.setLore(List.of(String.valueOf(i)));
                is.setItemMeta(meta);
            }
            Item item = e.getEntity().getWorld().dropItemNaturally(e.getEntity().getEyeLocation(), is);
            item.setPickupDelay(Integer.MAX_VALUE);
            item.setVelocity(new Vector(random.nextDouble() * 0.1, 0.4, random.nextDouble() * 0.1));
            bloodItems.put(item, System.currentTimeMillis());
        }
    }

    private boolean showBlood(Entity entity, EntityDamageEvent.DamageCause damageCause, double damage) {
        return ConfigUtil.isShowIndicator(entity, damageCause, damage, DISABLED_BLOOD, enabled, enablePlayer, sneaking, enableMonster, enableAnimal, disabledEntities, disabledDamageCauses);
    }

    private void checkBloodItem(Item item, Cancellable cancellable) {
        if (bloodItems.containsKey(item)) {
            cancellable.setCancelled(true);
            return;
        }
        ItemStack itemStack = item.getItemStack();
        if (item.getPickupDelay() == Integer.MAX_VALUE && itemStack.hasItemMeta()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta.hasDisplayName() && itemMeta.getDisplayName().equals(BLOOD_NAME)) {
                item.remove();
            }
        }
    }

    public Map<Item, Long> getBloodItems() {
        return bloodItems;
    }
}
