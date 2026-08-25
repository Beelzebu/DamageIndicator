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
package cl.mastercode.DamageIndicator.storage;

import cl.mastercode.DamageIndicator.DIMain;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 * @author Beelzebu
 */
public class SimpleStorageProvider implements StorageProvider {

    private final File dataFile = new File(DIMain.getPlugin(DIMain.class).getDataFolder(), "data.yml");
    private final FileConfiguration data;

    public SimpleStorageProvider() {
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(SimpleStorageProvider.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    @Override
    public boolean showArmorStand(Player player) {
        return showDamageIndicator(player);
    }

    @Override
    public void setShowArmorStand(Player player, boolean status) {
        setShowDamageIndicator(player, status);
    }

    @Override
    public boolean showDamageIndicator(Player player) {
        return data.getBoolean(player.getUniqueId().toString(), true);

    }

    @Override
    public void setShowDamageIndicator(Player player, boolean status) {
        data.set(player.getUniqueId().toString(), status);
        save();
    }

    private void save() {
        try {
            data.save(dataFile);
        } catch (IOException ex) {
            Logger.getLogger(SimpleStorageProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
