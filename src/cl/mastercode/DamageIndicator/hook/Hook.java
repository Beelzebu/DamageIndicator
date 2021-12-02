package cl.mastercode.DamageIndicator.hook;

import org.bukkit.event.entity.EntityDamageEvent;

/**
 * @author Beelzebu
 */
public interface Hook {

    String getPluginName();

    boolean isCritic(EntityDamageEvent e);
}
