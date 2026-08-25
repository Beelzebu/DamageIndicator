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
import cl.mastercode.DamageIndicator.hook.HookManager;
import cl.mastercode.DamageIndicator.util.ConfigUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.metadata.FixedMetadataValue;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author YitanTribal, Beelzebu
 */
public class DamageIndicatorListener implements Listener {

    private static final String DISABLED_DI = "DI-DISABLED-DI";
    private final DIMain plugin;
    private final Map<TextDisplay, Long> damageIndicators = new LinkedHashMap<>();
    private final Set<EntityType> disabledEntities = new HashSet<>();
    private final Set<CreatureSpawnEvent.SpawnReason> disabledSpawnReasons = new HashSet<>();
    private final Set<EntityDamageEvent.DamageCause> disabledDamageCauses = new HashSet<>();
    private final FixedMetadataValue damageIndicatorMeta;
    private final HookManager hookManager;
    private boolean enabled = true;
    private boolean enablePlayer = true;
    private boolean enableMonster = true;
    private boolean enableAnimal = true;
    private boolean sneaking = false;

    public DamageIndicatorListener(DIMain plugin, HookManager hookManager) {
        this.plugin = plugin;
        this.hookManager = hookManager;
        damageIndicatorMeta = new FixedMetadataValue(plugin, 0);
        reload();
    }

    public void reload() {
        disabledEntities.clear();
        disabledSpawnReasons.clear();
        enabled = plugin.getConfig().getBoolean("Damage Indicator.Enabled");
        enablePlayer = plugin.getConfig().getBoolean("Damage Indicator.Player");
        enableMonster = plugin.getConfig().getBoolean("Damage Indicator.Monster");
        enableAnimal = plugin.getConfig().getBoolean("Damage Indicator.Animals");
        sneaking = plugin.getConfig().getBoolean("Damage Indicator.Sneaking");
        plugin.getConfig().getStringList("Damage Indicator.Disabled Entities").stream().map(entity -> {
            try {
                return EntityType.valueOf(entity.toUpperCase());
            } catch (IllegalArgumentException e) {
                Logger.getLogger(DIMain.class.getName()).log(Level.WARNING, entity.toUpperCase() + " is not a valid EntityType.");
                return null;
            }
        }).filter(Objects::nonNull).forEach(disabledEntities::add);
        plugin.getConfig().getStringList("Damage Indicator.Disabled Spawn Reasons").stream().map(reason -> {
            try {
                return CreatureSpawnEvent.SpawnReason.valueOf(reason.toUpperCase());
            } catch (IllegalArgumentException e) {
                Logger.getLogger(DIMain.class.getName()).log(Level.WARNING, reason.toUpperCase() + " is not a valid SpawnReason.");
                return null;
            }
        }).filter(Objects::nonNull).forEach(disabledSpawnReasons::add);
        plugin.getConfig().getStringList("Damage Indicator.Disabled Damage Causes").stream().map(cause -> {
            try {
                return EntityDamageEvent.DamageCause.valueOf(cause);
            } catch (IllegalArgumentException e) {
                Logger.getLogger(DIMain.class.getName()).log(Level.WARNING, cause.toUpperCase() + " is not a valid DamageCause.");
                return null;
            }
        }).filter(Objects::nonNull).forEach(disabledDamageCauses::add);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (e.isCancelled()) {
            if (plugin.isDamageIndicator(e.getEntity())) {
                e.setCancelled(false);
            }
            return;
        }
        if (!isSpawnDamageIndicator(e.getEntity(), null, .1)) {
            return;
        }
        if (disabledSpawnReasons.contains(e.getSpawnReason())) {
            e.getEntity().setMetadata(DISABLED_DI, new FixedMetadataValue(plugin, 1));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void oneEntitySpawn(EntitySpawnEvent e) {
        if (e.isCancelled() && e.getEntity() instanceof TextDisplay) {
            if (plugin.isDamageIndicator(e.getEntity())) {
                e.setCancelled(false);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof TextDisplay display) {
                if (plugin.isDamageIndicator(display)) {
                    display.remove();
                    damageIndicators.remove(display);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof TextDisplay display) {
                if (plugin.isDamageIndicator(display)) {
                    display.remove();
                    damageIndicators.remove(display);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityRegainHealth(EntityRegainHealthEvent e) {
        if (!(e.getEntity() instanceof LivingEntity livingEntity)) {
            return;
        }
        if (e.getEntity() instanceof Player player) {
            if (player.isSneaking() && !sneaking) {
                return;
            }
        }
        if (livingEntity.getHealth() == Objects.requireNonNull(livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue()) {
            return;
        }
        if (!e.isCancelled()) {
            handleDamageIndicator(livingEntity, e.getAmount());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageEvent(EntityDamageEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (!(e.getEntity() instanceof LivingEntity)) {
            return;
        }
        handleDamageIndicator((LivingEntity) e.getEntity(), e.getCause(), e.getFinalDamage(), hookManager.isCritic(e));
    }

    private String damageFormat(double damage) {
        DecimalFormat df;
        try {
            df = new DecimalFormat(Objects.requireNonNull(plugin.getConfig().getString("Damage Indicator.Format.Decimal", "#.##")));
        } catch (Exception ex) {
            df = new DecimalFormat("#.##");
        }
        return df.format(damage);
    }

    private void handleDamageIndicator(LivingEntity entity, double health) {
        if (isSpawnDamageIndicator(entity, null, health)) {
            spawnDamageIndicator(entity.getLocation(), plugin.getConfig().getString("Damage Indicator.Format.EntityRegain", "").replace("%health%", damageFormat(health)));
        }
    }

    private void handleDamageIndicator(LivingEntity entity, EntityDamageEvent.DamageCause damageCause, double damage, boolean crit) {
        if (isSpawnDamageIndicator(entity, damageCause, damage)) {
            if (!crit) {
                spawnDamageIndicator(entity.getLocation(), plugin.getConfig().getString("Damage Indicator.Format.EntityDamage", "").replace("%damage%", damageFormat(damage)));
            } else {
                spawnDamageIndicator(entity.getLocation(), plugin.getConfig().getString("Damage Indicator.Format.EntityDamage", "").replace("%damage%", damageFormat(damage)) + "&r ✧");
            }
        }
    }

    public TextDisplay spawnDamageIndicator(Location loc, String name) {
        TextDisplay display = buildTextDisplay(loc, plugin.getConfig().getDouble("Damage Indicator.Distance"), damageIndicatorMeta, name);
        if (plugin.getEntityHider() != null) {
            Bukkit.getOnlinePlayers().stream().filter(op -> !plugin.getStorageProvider().showDamageIndicator(op)).forEach(op -> plugin.getEntityHider().hideEntity(op, display));
        }
        damageIndicators.put(display, System.currentTimeMillis());
        return display;
    }

    private boolean isSpawnDamageIndicator(Entity entity, EntityDamageEvent.DamageCause damageCause, double damage) {
        return ConfigUtil.isShowIndicator(entity, damageCause, damage, DISABLED_DI, enabled, enablePlayer, sneaking, enableMonster, enableAnimal, disabledEntities, disabledDamageCauses);
    }

    public Map<TextDisplay, Long> getDamageIndicators() {
        return damageIndicators;
    }

    private TextDisplay buildTextDisplay(Location location, double distance, FixedMetadataValue fixedMetadataValue, String name) {
        return location.getWorld().spawn(location.clone().add(0, distance, 0), TextDisplay.class, stand -> setupDamageIndicator(stand, fixedMetadataValue, name));
    }

    private void setupDamageIndicator(TextDisplay textDisplay, FixedMetadataValue fixedMetadataValue, String name) {
        textDisplay.setMetadata("Mastercode-DamageIndicator", fixedMetadataValue);
        textDisplay.text(MiniMessage.miniMessage().deserialize(name));
        textDisplay.setBillboard(Display.Billboard.CENTER);
        textDisplay.setAlignment(TextDisplay.TextAlignment.CENTER);
        textDisplay.setShadowed(false);
        textDisplay.setViewRange(5);
        textDisplay.setPersistent(false);
        textDisplay.setTeleportDuration(1);
        textDisplay.setInterpolationDelay(0);
        textDisplay.setTextOpacity((byte) 0);
    }
}
