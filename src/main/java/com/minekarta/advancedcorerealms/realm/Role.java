package com.minekarta.advancedcorerealms.realm;

/**
 * Represents the role of a player within a realm.
 */
public enum Role {
    /**
     * The owner of the realm, with full permissions.
     */
    OWNER,
    /**
     * An administrator with elevated permissions, but less than the owner.
     */
    ADMIN,
    /**
     * A member with standard build and interaction permissions.
     */
    MEMBER,
    /**
     * A visitor with limited, typically read-only, permissions.
     */
    VISITOR
}