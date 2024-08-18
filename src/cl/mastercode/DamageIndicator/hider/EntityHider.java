package cl.mastercode.DamageIndicator.hider;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.Objects;

public abstract class EntityHider implements Listener, AutoCloseable {

    public abstract boolean hideEntity(Player observer, Entity entity);

    public abstract boolean showEntity(Player observer, Entity entity);

    public abstract boolean toggleEntity(Player observer, Entity entity);

    protected final void validate(Player observer, Entity entity) {
        Objects.requireNonNull(observer, "observer cannot be NULL.");
        Objects.requireNonNull(entity, "entity cannot be NULL.");
    }

    @Override
    public void close() throws Exception {
        HandlerList.unregisterAll(this);
    }
}
