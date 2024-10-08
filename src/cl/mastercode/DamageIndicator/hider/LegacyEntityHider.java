package cl.mastercode.DamageIndicator.hider;

import com.comphenix.protocol.PacketType;
import static com.comphenix.protocol.PacketType.Play.Server.ANIMATION;
import static com.comphenix.protocol.PacketType.Play.Server.ATTACH_ENTITY;
import static com.comphenix.protocol.PacketType.Play.Server.BED;
import static com.comphenix.protocol.PacketType.Play.Server.BLOCK_BREAK_ANIMATION;
import static com.comphenix.protocol.PacketType.Play.Server.COLLECT;
import static com.comphenix.protocol.PacketType.Play.Server.COMBAT_EVENT;
import static com.comphenix.protocol.PacketType.Play.Server.ENTITY_DESTROY;
import static com.comphenix.protocol.PacketType.Play.Server.ENTITY_EFFECT;
import static com.comphenix.protocol.PacketType.Play.Server.ENTITY_EQUIPMENT;
import static com.comphenix.protocol.PacketType.Play.Server.ENTITY_HEAD_ROTATION;
import static com.comphenix.protocol.PacketType.Play.Server.ENTITY_LOOK;
import static com.comphenix.protocol.PacketType.Play.Server.ENTITY_METADATA;
import static com.comphenix.protocol.PacketType.Play.Server.ENTITY_MOVE_LOOK;
import static com.comphenix.protocol.PacketType.Play.Server.ENTITY_STATUS;
import static com.comphenix.protocol.PacketType.Play.Server.ENTITY_TELEPORT;
import static com.comphenix.protocol.PacketType.Play.Server.ENTITY_VELOCITY;
import static com.comphenix.protocol.PacketType.Play.Server.NAMED_ENTITY_SPAWN;
import static com.comphenix.protocol.PacketType.Play.Server.PLAYER_COMBAT_END;
import static com.comphenix.protocol.PacketType.Play.Server.PLAYER_COMBAT_ENTER;
import static com.comphenix.protocol.PacketType.Play.Server.PLAYER_COMBAT_KILL;
import static com.comphenix.protocol.PacketType.Play.Server.REL_ENTITY_MOVE;
import static com.comphenix.protocol.PacketType.Play.Server.REL_ENTITY_MOVE_LOOK;
import static com.comphenix.protocol.PacketType.Play.Server.REMOVE_ENTITY_EFFECT;
import static com.comphenix.protocol.PacketType.Play.Server.SPAWN_ENTITY;
import static com.comphenix.protocol.PacketType.Play.Server.SPAWN_ENTITY_EXPERIENCE_ORB;
import static com.comphenix.protocol.PacketType.Play.Server.SPAWN_ENTITY_LIVING;
import static com.comphenix.protocol.PacketType.Play.Server.SPAWN_ENTITY_PAINTING;
import static com.comphenix.protocol.PacketType.Play.Server.UPDATE_ENTITY_NBT;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.Plugin;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Original source: <a href="https://gist.github.com/dmulloy2/5526f5bf906c064c255e">https://gist.github.com/dmulloy2/5526f5bf906c064c255e</a>
 *
 * @author aadnk
 */
public class LegacyEntityHider extends EntityHider {
    // Packets that update remote player entities
    private static final PacketType[] ENTITY_PACKETS = {
            ENTITY_EQUIPMENT,
            BED,
            ANIMATION,
            NAMED_ENTITY_SPAWN,
            COLLECT,
            SPAWN_ENTITY,
            SPAWN_ENTITY_LIVING,
            SPAWN_ENTITY_PAINTING,
            SPAWN_ENTITY_EXPERIENCE_ORB,
            ENTITY_VELOCITY,
            REL_ENTITY_MOVE,
            ENTITY_LOOK,
            ENTITY_MOVE_LOOK,
            ENTITY_MOVE_LOOK,
            ENTITY_TELEPORT,
            ENTITY_HEAD_ROTATION,
            ENTITY_STATUS,
            ATTACH_ENTITY,
            ENTITY_METADATA,
            ENTITY_EFFECT,
            REMOVE_ENTITY_EFFECT,
            BLOCK_BREAK_ANIMATION,
            UPDATE_ENTITY_NBT,
            COMBAT_EVENT,
            REL_ENTITY_MOVE,
            REL_ENTITY_MOVE_LOOK,
            PLAYER_COMBAT_END,
            PLAYER_COMBAT_KILL,
            PLAYER_COMBAT_ENTER
            // We don't handle DESTROY_ENTITY though
    };
    // Current policy
    private final Policy policy;
    // Listeners
    private final PacketAdapter protocolListener;
    private final Table<Integer, Integer, Boolean> observerEntityMap = HashBasedTable.create();
    private ProtocolManager manager;

    /**
     * Construct a new entity hider.
     *
     * @param plugin - the plugin that controls this entity hider.
     * @param policy - the default visibility policy.
     */
    public LegacyEntityHider(Plugin plugin, Policy policy) {
        Objects.requireNonNull(plugin, "plugin cannot be NULL.");

        // Save policy
        this.policy = policy;
        this.manager = ProtocolLibrary.getProtocolManager();

        // Register events and packet listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        manager.addPacketListener(protocolListener = constructProtocol(plugin));
    }

    /**
     * Set the visibility status of a given entity for a particular observer.
     *
     * @param observer - the observer player.
     * @param entityID - ID of the entity that will be hidden or made visible.
     * @param visible  - TRUE if the entity should be made visible, FALSE if not.
     * @return TRUE if the entity was visible before this method call, FALSE otherwise.
     */
    private boolean setVisibility(Player observer, int entityID, boolean visible) {
        switch (policy) {
            case BLACKLIST:
                // Non-membership means they are visible
                return !setMembership(observer, entityID, !visible);
            case WHITELIST:
                return setMembership(observer, entityID, visible);
            default:
                throw new IllegalArgumentException("Unknown policy: " + policy);
        }
    }

    /**
     * Add or remove the given entity and observer entry from the table.
     *
     * @param observer - the player observer.
     * @param entityID - ID of the entity.
     * @param member   - TRUE if they should be present in the table, FALSE otherwise.
     * @return TRUE if they already were present, FALSE otherwise.
     */
    // Helper method
    private boolean setMembership(Player observer, int entityID, boolean member) {
        if (member) {
            return observerEntityMap.put(observer.getEntityId(), entityID, true) != null;
        } else {
            return observerEntityMap.remove(observer.getEntityId(), entityID) != null;
        }
    }

    /**
     * Determine if the given entity and observer is present in the table.
     *
     * @param observer - the player observer.
     * @param entityID - ID of the entity.
     * @return TRUE if they are present, FALSE otherwise.
     */
    private boolean getMembership(Player observer, int entityID) {
        return observerEntityMap.contains(observer.getEntityId(), entityID);
    }

    /**
     * Determine if a given entity is visible for a particular observer.
     *
     * @param observer - the observer player.
     * @param entityID -  ID of the entity that we are testing for visibility.
     * @return TRUE if the entity is visible, FALSE otherwise.
     */
    private boolean isVisible(Player observer, int entityID) {
        // If we are using a whitelist, presence means visibility - if not, the opposite is the case
        boolean presence = getMembership(observer, entityID);

        return (policy == Policy.WHITELIST) == presence;
    }

    /**
     * Remove the given entity from the underlying map.
     *
     * @param entity    - the entity to remove.
     * @param destroyed - TRUE if the entity was killed, FALSE if it is merely unloading.
     */
    private void removeEntity(Entity entity, boolean destroyed) {
        int entityID = entity.getEntityId();

        for (Map<Integer, Boolean> maps : observerEntityMap.rowMap().values()) {
            maps.remove(entityID);
        }
    }

    /**
     * Invoked when a player logs out.
     *
     * @param player - the player that jused logged out.
     */
    private void removePlayer(org.bukkit.entity.Player player) {
        // Cleanup
        observerEntityMap.rowMap().remove(player.getEntityId());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        removeEntity(e.getEntity(), true);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        for (Entity entity : e.getChunk().getEntities()) {
            removeEntity(entity, false);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        removePlayer(e.getPlayer());
    }

    /**
     * Construct the packet listener that will be used to intercept every entity-related packet.
     *
     * @param plugin - the parent plugin.
     * @return The packet listener.
     */
    private PacketAdapter constructProtocol(Plugin plugin) {
        return new PacketAdapter(plugin, ENTITY_PACKETS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                int index = event.getPacketType() == COMBAT_EVENT ? 1 : 0;

                Integer entityID = event.getPacket().getIntegers().readSafely(index);
                if (entityID != null) {
                    if (!isVisible(event.getPlayer(), entityID)) {
                        event.setCancelled(true);
                    }
                }
            }
        };
    }

    /**
     * Toggle the visibility status of an entity for a player.
     * <p>
     * If the entity is visible, it will be hidden. If it is hidden, it will become visible.
     *
     * @param observer - the player observer.
     * @param entity   - the entity to toggle.
     * @return TRUE if the entity was visible before, FALSE otherwise.
     */
    @Override
    public final boolean toggleEntity(Player observer, Entity entity) {
        if (isVisible(observer, entity.getEntityId())) {
            return hideEntity(observer, entity);
        } else {
            return !showEntity(observer, entity);
        }
    }

    /**
     * Allow the observer to see an entity that was previously hidden.
     *
     * @param observer - the observer.
     * @param entity   - the entity to show.
     * @return TRUE if the entity was hidden before, FALSE otherwise.
     */
    @Override
    public final boolean showEntity(Player observer, Entity entity) {
        validate(observer, entity);
        boolean hiddenBefore = !setVisibility(observer, entity.getEntityId(), true);

        // Resend packets
        if (manager != null && hiddenBefore) {
            manager.updateEntity(entity, Collections.singletonList(observer));
        }
        return hiddenBefore;
    }

    /**
     * Prevent the observer from seeing a given entity.
     *
     * @param observer - the player observer.
     * @param entity   - the entity to hide.
     * @return TRUE if the entity was previously visible, FALSE otherwise.
     */
    @Override
    public final boolean hideEntity(Player observer, Entity entity) {
        validate(observer, entity);
        boolean visibleBefore = setVisibility(observer, entity.getEntityId(), false);

        if (visibleBefore) {
            PacketContainer destroyEntity = new PacketContainer(ENTITY_DESTROY);
            destroyEntity.getIntegerArrays().write(0, new int[]{entity.getEntityId()});

            // Make the entity disappear
            manager.sendServerPacket(observer, destroyEntity);
        }
        return visibleBefore;
    }

    /**
     * Determine if the given entity has been hidden from an observer.
     * <p>
     * Note that the entity may very well be occluded or out of range from the perspective
     * of the observer. This method simply checks if an entity has been completely hidden
     * for that observer.
     *
     * @param observer - the observer.
     * @param entity   - the entity that may be hidden.
     * @return TRUE if the player may see the entity, FALSE if the entity has been hidden.
     */
    public final boolean canSee(Player observer, Entity entity) {
        validate(observer, entity);

        return isVisible(observer, entity.getEntityId());
    }

    /**
     * Retrieve the current visibility policy.
     *
     * @return The current visibility policy.
     */
    public Policy getPolicy() {
        return policy;
    }

    @Override
    public void close() throws Exception {
        super.close();
        if (manager != null) {
            HandlerList.unregisterAll(this);
            manager.removePacketListener(protocolListener);
            manager = null;
        }
    }
}
