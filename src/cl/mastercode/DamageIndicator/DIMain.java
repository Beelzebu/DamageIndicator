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
import cl.mastercode.DamageIndicator.hider.Policy;
import cl.mastercode.DamageIndicator.hider.SpigotEntityHider;
import cl.mastercode.DamageIndicator.hook.HookManager;
import cl.mastercode.DamageIndicator.listener.BloodListener;
import cl.mastercode.DamageIndicator.listener.DamageIndicatorListener;
import cl.mastercode.DamageIndicator.storage.SimpleStorageProvider;
import cl.mastercode.DamageIndicator.storage.StorageProvider;
import cl.mastercode.DamageIndicator.util.ConfigUpdateHandler;
import net.nifheim.bukkit.commandlib.CommandAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.scheduler.BukkitTask;

/**
 * @author YitanTribal, Beelzebu
 */
public class DIMain extends JavaPlugin {

    private EntityHider entityHider = new SpigotEntityHider(this, Policy.BLACKLIST);
    private DamageIndicatorListener damageIndicatorListener;
    private BloodListener bloodListener;
    private StorageProvider storageProvider = null;
    private DamageIndicatorCommand command;
    private FileConfiguration messages;
    private BukkitTask task;

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
            damageIndicatorListener.getDamageIndicators().forEach((armor, time) -> armor.remove());
            damageIndicatorListener.getDamageIndicators().clear();
        }
        // remove blood
        if (bloodListener != null) {
            bloodListener.getBloodItems().forEach((item, time) -> item.remove());
            bloodListener.getBloodItems().clear();
        }
        if (getConfig().getBoolean("Damage Indicator.Enabled")) {
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
        saveResource("messages.yml", false);
        reload();
        startTasks();
    }

    @Override
    public void onDisable() {
        if (damageIndicatorListener != null) {
            damageIndicatorListener.getDamageIndicators().forEach((armor, time) -> armor.remove());
        }
        if (bloodListener != null) {
            bloodListener.getBloodItems().forEach((item, time) -> item.remove());
        }
        if (task != null) {
            task.cancel();
        }
    }

    private void startTasks() {
        final long damageIndicatorDuration = getConfig().getInt("Damage Indicator.Duration", 30) * 50L;
        final long bloodIndicatorDuration = getConfig().getInt("Blood Indicator.Duration", 30) * 50L;
        task = Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (damageIndicatorListener != null) {
                Iterator<Map.Entry<TextDisplay, Long>> iterator = damageIndicatorListener.getDamageIndicators().entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<TextDisplay, Long> entry = iterator.next();
                    TextDisplay textDisplay = entry.getKey();
                    Long time = entry.getValue();
                    if (time + damageIndicatorDuration <= System.currentTimeMillis()) {
                        textDisplay.remove();
                        iterator.remove();
                    } else {
                        textDisplay.teleport(textDisplay.getLocation().add(0.0, 0.07, 0.0));
                    }
                }
            }
            if (bloodListener != null) {
                Iterator<Map.Entry<Item, Long>> iterator = bloodListener.getBloodItems().entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Item, Long> entry = iterator.next();
                    Item item = entry.getKey();
                    Long time = entry.getValue();
                    if (time + bloodIndicatorDuration <= System.currentTimeMillis()) {
                        item.remove();
                        iterator.remove();
                    }
                }
            }
        }, 0, 1);
    }

    public boolean isDamageIndicator(Entity entity) {
        if (entity instanceof TextDisplay display && entity.isValid()) {
            return display.hasMetadata("Mastercode-DamageIndicator");
        }
        return false;
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
}
