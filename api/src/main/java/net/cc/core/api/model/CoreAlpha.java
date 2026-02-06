package net.cc.core.api.model;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

/**
 * Model enum for an alpha role.
 *
 * @since 1.1
 */
public enum CoreAlpha {
    PLOTS_ALPHA(NamedTextColor.AQUA),
    EARTH_ALPHA(NamedTextColor.GREEN),
    ISLANDS_ALPHA(NamedTextColor.RED),
    LEGACY_ALPHA(NamedTextColor.DARK_PURPLE);

    final TextColor color;

    // Constructor
    CoreAlpha(final TextColor color) {
        this.color = color;
    }

    public TextColor getColor() {
        return this.color;
    }

}
