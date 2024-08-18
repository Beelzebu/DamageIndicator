package cl.mastercode.DamageIndicator.hider;

import cl.mastercode.DamageIndicator.DIMain;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class SpigotEntityHider extends EntityHider {

    private final DIMain plugin;
    private final Policy policy;

    public SpigotEntityHider(DIMain plugin, Policy policy) {
        this.plugin = plugin;
        this.policy = policy;
    }

    @Override
    public boolean hideEntity(Player observer, Entity entity) {
        validate(observer, entity);
        observer.hideEntity(plugin, entity);
        return observer.canSee(entity) & policy == Policy.WHITELIST;
    }

    @Override
    public boolean showEntity(Player observer, Entity entity) {
        validate(observer, entity);
        observer.showEntity(plugin, entity);
        return observer.canSee(entity) & policy == Policy.WHITELIST;
    }

    @Override
    public boolean toggleEntity(Player observer, Entity entity) {
        if (observer.canSee(entity)) {
            observer.hideEntity(plugin, entity);
        } else {
            observer.showEntity(plugin, entity);
        }
        return observer.canSee(entity);
    }
}
