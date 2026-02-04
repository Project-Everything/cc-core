package net.cc.core.api;

import org.jetbrains.annotations.ApiStatus;

/**
 * Static accessor for the {@link Core} instance.
 *
 * @since 1.0
 */
public final class CoreProvider {

    private static Core instance = null;

    public static Core get() {
        final Core instance = CoreProvider.instance;

        if (instance == null) {
            throw new IllegalStateException("cc-core not initialized!");
        }

        return instance;
    }

    @ApiStatus.Internal
    public static void register(final Core instance) {
        CoreProvider.instance = instance;
    }

    @ApiStatus.Internal
    public static void unregister() {
        CoreProvider.instance = null;
    }

    @ApiStatus.Internal
    private CoreProvider() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }
}
