package net.cc.core.api.model;

/**
 * Model enum for a permission group.
 *
 * @since 1.2
 */
public enum CoreGroup {
    OWNER("owner"),
    ADMIN("admin"),
    DEV("dev"),
    MOD("mod"),
    HELPER("helper"),
    CONTENT_TEAM("content-team"),
    SPONSOR("sponsor"),
    DEFAULT("default");

    final String key;

    // Constructor
    CoreGroup(final String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

}
