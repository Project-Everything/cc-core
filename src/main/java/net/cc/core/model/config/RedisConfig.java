package net.cc.core.model.config;

import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 * Model class for Redis settings.
 *
 * @since 1.0.0
 */
@Getter
@ConfigSerializable
public final class RedisConfig {

    private final String host;
    private final int port;
    private final String password;

    // Constructor
    public RedisConfig() {
        this.host = "127.0.0.1";
        this.port = 6379;
        this.password = "password";
    }
}