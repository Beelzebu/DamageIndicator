package cl.mastercode.DamageIndicator.hook;

import cl.mastercode.DamageIndicator.DIMain;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * @author Beelzebu
 */
public class HookManager {

    private final Set<Hook> hooks = new HashSet<>();

    public HookManager(DIMain plugin) {
        if (Bukkit.getPluginManager().getPlugin("EcoSkills") != null) {
            hooks.add(new EcoSkillsHook());
        }
        for (Hook hook : hooks) {
            plugin.getLogger().info("Registered hook with: " + hook.getPluginName());
        }
    }

    public boolean isCritic(EntityDamageEvent e) {
        for (Hook hook : hooks) {
            if (hook.isCritic(e)) {
                return true;
            }
        }
        return false;
    }
}
