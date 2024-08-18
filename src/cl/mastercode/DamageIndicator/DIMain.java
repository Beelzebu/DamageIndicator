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
package cl.mastercode.DamageIndicator;

import cl.mastercode.DamageIndicator.command.DamageIndicatorCommand;
import cl.mastercode.DamageIndicator.hider.EntityHider;
import cl.mastercode.DamageIndicator.hider.LegacyEntityHider;
import cl.mastercode.DamageIndicator.hider.Policy;
import cl.mastercode.DamageIndicator.hider.SpigotEntityHider;
import cl.mastercode.DamageIndicator.hook.HookManager;
import cl.mastercode.DamageIndicator.listener.BloodListener;
import cl.mastercode.DamageIndicator.listener.DamageIndicatorListener;
import cl.mastercode.DamageIndicator.storage.SimpleStorageProvider;
import cl.mastercode.DamageIndicator.storage.StorageProvider;
import cl.mastercode.DamageIndicator.util.CompatUtil;
import cl.mastercode.DamageIndicator.util.ConfigUpdateHandler;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.nifheim.bukkit.commandlib.CommandAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import java.util.Iterator;
import java.util.Map;

/**
 * @author YitanTribal, Beelzebu
 */
public class DIMain extends JavaPlugin {

    private DamageIndicatorListener damageIndicatorListener;
    private BloodListener bloodListener;
    private StorageProvider storageProvider = null;
    private EntityHider entityHider;
    private DamageIndicatorCommand command;
    private FileConfiguration messages;
    private BukkitAudiences adventure;

    public void reload() {
        new ConfigUpdateHandler(this).updateConfig();
        reloadConfig();
        if (storageProvider == null) {
            storageProvider = new SimpleStorageProvider();
        }
        // unregister command
        if (command != null) {
            CommandAPI.unregisterCommand(this, command);
            command = null;
        }
        // remove armor stands
        if (damageIndicatorListener != null) {
            damageIndicatorListener.getArmorStands().forEach((armor, time) -> armor.remove());
            damageIndicatorListener.getArmorStands().clear();
        }
        // remove blood
        if (bloodListener != null) {
            bloodListener.getBloodItems().forEach((item, time) -> item.remove());
            bloodListener.getBloodItems().clear();
        }
        if (getConfig().getBoolean("Damage Indicator.Enabled")) {
            if (CompatUtil.MINOR_VERSION >= 18) { // 1.18 added an entity hider
                getLogger().info("Version 1.18 or higher detected, trying to use SpigotEntityHider for per-player damage indicators.");
                try {
                    Player.class.getDeclaredMethod("canSee", Entity.class);
                    entityHider = new SpigotEntityHider(this, Policy.BLACKLIST);
                } catch (ReflectiveOperationException e) {
                    getLogger().info("Your spigot version seems outdated, please check for updates with /version command!");
                }
            }
            if (entityHider == null && Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
                getLogger().info("ProtocolLib found, trying to enable LegacyEntityHider for per-player damage indicators.");
                entityHider = new LegacyEntityHider(this, Policy.BLACKLIST);
            }
            if (damageIndicatorListener == null) {
                Bukkit.getPluginManager().registerEvents(damageIndicatorListener = new DamageIndicatorListener(this, new HookManager(this)), this);
            }
            damageIndicatorListener.reload();
        } else if (damageIndicatorListener != null) {
            getLogger().info("Damage Indicators were enabled and now is marked as disabled, we'll try to disable this feature.");
            HandlerList.unregisterAll(damageIndicatorListener);
            damageIndicatorListener = null;
        }
        if (getConfig().getBoolean("Blood.Enabled")) {
            if (bloodListener == null) {
                Bukkit.getPluginManager().registerEvents(bloodListener = new BloodListener(this), this);
            }
            bloodListener.reload();
        } else if (bloodListener != null) {
            getLogger().info("Blood was enabled and now is marked as disabled, we'll try to disable this feature.");
            HandlerList.unregisterAll(bloodListener);
            bloodListener = null;
        }
        if (command == null) {
            command = new DamageIndicatorCommand(this);
            CommandAPI.registerCommand(this, command);
        }
        reloadMessages();
    }

    @Override
    public void onEnable() {
        this.adventure = BukkitAudiences.create(this);
        saveResource("messages.yml", false);
        CompatUtil.onEnable();
        reload();
        startTasks();
    }

    @Override
    public void onDisable() {
        if (damageIndicatorListener != null) {
            damageIndicatorListener.getArmorStands().forEach((armor, time) -> armor.remove());
        }
        if (bloodListener != null) {
            bloodListener.getBloodItems().forEach((item, time) -> item.remove());
        }
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    private void startTasks() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (damageIndicatorListener != null) {
                Iterator<Map.Entry<ArmorStand, Long>> asit = damageIndicatorListener.getArmorStands().entrySet().iterator();
                while (asit.hasNext()) {
                    Map.Entry<ArmorStand, Long> ent = asit.next();
                    if (ent.getValue() + 1500 <= System.currentTimeMillis()) {
                        ent.getKey().remove();
                        asit.remove();
                    } else {
                        ent.getKey().teleport(ent.getKey().getLocation().clone().add(0.0, 0.07, 0.0));
                    }
                }
            }
            if (bloodListener != null) {
                Iterator<Map.Entry<Item, Long>> bit = bloodListener.getBloodItems().entrySet().iterator();
                while (bit.hasNext()) {
                    Map.Entry<Item, Long> ent = bit.next();
                    if (ent.getValue() + 2000 <= System.currentTimeMillis()) {
                        ent.getKey().remove();
                        bit.remove();
                    }
                }
            }
        }, 0, 1);
    }

    public boolean isDamageIndicator(Entity entity) {
        ArmorStand as = (ArmorStand) entity;
        return as.hasMetadata("Mastercode-DamageIndicator") && as.isMarker() && !as.isVisible() && as.isCustomNameVisible() && !as.hasGravity();
    }

    public StorageProvider getStorageProvider() {
        return storageProvider;
    }

    public void setStorageProvider(StorageProvider storageProvider) {
        this.storageProvider = storageProvider;
    }

    public EntityHider getEntityHider() {
        return entityHider;
    }

    public void setEntityHider(EntityHider entityHider) {
        this.entityHider = entityHider;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public void reloadMessages() {
        messages = YamlConfiguration.loadConfiguration(getDataFolder().toPath().resolve("messages.yml").toFile());
    }

    public @NonNull BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }
}
