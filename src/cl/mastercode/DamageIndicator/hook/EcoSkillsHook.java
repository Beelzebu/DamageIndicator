package cl.mastercode.DamageIndicator.hook;

import com.willfp.ecoskills.EcoSkillsEventModifierHandlerKt;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * @author Beelzebu
 */
public class EcoSkillsHook implements Hook {

    @Override
    public String getPluginName() {
        return "EcoSkills";
    }

    @Override
    public boolean isCritic(EntityDamageEvent e) {
        return e instanceof EntityDamageByEntityEvent && EcoSkillsEventModifierHandlerKt.isCrit((EntityDamageByEntityEvent) e);
    }
}
