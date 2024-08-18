package cl.mastercode.DamageIndicator.hider;

/**
 * The current entity visibility policy.
 *
 * @author Kristian
 */
public enum Policy {
    /**
     * All entities are invisible by default. Only entities specifically made visible may be seen.
     */
    WHITELIST,

    /**
     * All entities are visible by default. An entity can only be hidden explicitly.
     */
    BLACKLIST,
}