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

import cl.mastercode.DamageIndicator.command.CommandHandler;
import cl.mastercode.DamageIndicator.hook.HookManager;
import cl.mastercode.DamageIndicator.listener.BloodListener;
import cl.mastercode.DamageIndicator.listener.DamageIndicatorListener;
import cl.mastercode.DamageIndicator.storage.SimpleStorageProvider;
import cl.mastercode.DamageIndicator.storage.StorageProvider;
import cl.mastercode.DamageIndicator.util.CompatUtil;
import cl.mastercode.DamageIndicator.util.ConfigUpdateHandler;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author YitanTribal, Beelzebu
 */
public class DIMain extends JavaPlugin {

    private DamageIndicatorListener damageIndicatorListener;
    private BloodListener bloodListener;
    private StorageProvider storageProvider = null;

    public void reload() {
        new ConfigUpdateHandler(this).updateConfig();
        reloadConfig();
        if (storageProvider == null) {
            storageProvider = new SimpleStorageProvider();
        }
        if (damageIndicatorListener != null) {
            damageIndicatorListener.getArmorStands().forEach((armor, time) -> armor.remove());
            damageIndicatorListener.getArmorStands().clear();
            damageIndicatorListener.reload();
        } else if (getConfig().getBoolean("Damage Indicator.Enabled")) {
            Bukkit.getPluginManager().registerEvents(damageIndicatorListener = new DamageIndicatorListener(this, new HookManager(this)), this);
        }
        if (bloodListener != null) {
            bloodListener.getBloodItems().forEach((item, time) -> item.remove());
            bloodListener.getBloodItems().clear();
            bloodListener.reload();
        } else if (getConfig().getBoolean("Blood.Enabled")) {
            Bukkit.getPluginManager().registerEvents(bloodListener = new BloodListener(this), this);
        }
    }

    @Override
    public void onEnable() {
        reload();
        getCommand("damageindicator").setExecutor(new CommandHandler(this));
        startTasks();
        CompatUtil.onEnable();
    }

    @Override
    public void onDisable() {
        if (damageIndicatorListener != null) {
            damageIndicatorListener.getArmorStands().forEach((armor, time) -> armor.remove());
        }
        if (bloodListener != null) {
            bloodListener.getBloodItems().forEach((item, time) -> item.remove());
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
        return isDamageIndicator(entity, true);
    }

    public boolean isDamageIndicator(Entity entity, boolean strict) {
        if (!(entity instanceof ArmorStand)) {
            return false;
        }
        ArmorStand as = (ArmorStand) entity;
        return as.hasMetadata("Mastercode-DamageIndicator") || !strict && (as.isMarker() && !as.isVisible() && as.isCustomNameVisible() && !as.hasGravity());
    }

    public StorageProvider getStorageProvider() {
        return storageProvider;
    }

    public void setStorageProvider(StorageProvider storageProvider) {
        this.storageProvider = storageProvider;
    }
}
