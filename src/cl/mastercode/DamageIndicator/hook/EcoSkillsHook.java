package cl.mastercode.DamageIndicator.hook;

import com.willfp.ecoskills.skills.SkillCritsKt;
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
        return e instanceof EntityDamageByEntityEvent && SkillCritsKt.isSkillCrit((EntityDamageByEntityEvent) e);
    }
}
