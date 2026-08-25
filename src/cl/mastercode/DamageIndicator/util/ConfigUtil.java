package cl.mastercode.DamageIndicator.util;

import java.util.Set;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * @author Beelzebu
 */
public final class ConfigUtil {

    public static boolean isShowIndicator(Entity entity, EntityDamageEvent.DamageCause damageCause, double damage, String metadataTag, boolean enabled, boolean enablePlayer, boolean sneaking, boolean enableMonster, boolean enableAnimal, Set<EntityType> disabledEntities, Set<EntityDamageEvent.DamageCause> disabledDamageCauses) {
        /*
        Bukkit.broadcastMessage("Type: " + entity.getType() + "\n" +
                "Cause: " + damageCause + "\n" +
                "Damage: " + damage + "\n" +
                "Metadata: " + metadataTag + "\n" +
                "Enabled: " + enabled + "\n" +
                "Enabled Player: " + enablePlayer + "\n" +
                "Sneaking: " + sneaking + "\n" +
                "Enabled Monster: " + enableMonster + "\n" +
                "Enabled Animal: " + enableAnimal + "\n" +
                "Disabled Entities: " + disabledEntities + "\n" +
                "Disabled Damage Causes: " + disabledDamageCauses
        );
        */
        if (entity.hasMetadata(metadataTag)) {
            return false;
        }
        if (!enabled) {
            return false;
        }
        if (damage <= 0) {
            return false;
        }
        if (!(entity instanceof LivingEntity)) {
            return false;
        }
        if (entity instanceof ArmorStand) {
            return false;
        }
        if (entity instanceof TextDisplay) {
            return false;
        }
        if (entity instanceof Player player) {
            if (!enablePlayer) {
                return false;
            }
            if (player.isSneaking() && !sneaking) {
                return false;
            }
            if (entity.hasMetadata("NPC")) {
                return false;
            }
        }
        if ((entity instanceof Monster || entity instanceof Slime) && !enableMonster) {
            return false;
        }
        if (entity instanceof Animals && !enableAnimal) {
            return false;
        }
        if (disabledEntities.contains(entity.getType())) {
            return false;
        }
        return !disabledDamageCauses.contains(damageCause);
    }
}
