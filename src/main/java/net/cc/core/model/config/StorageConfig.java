package net.cc.core.model.config;

import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 * Model class for database settings.
 *
 * @since 1.0.0
 */
@Getter
@ConfigSerializable
public final class StorageConfig {

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    // Constructor
    public StorageConfig() {
        this.host = "127.0.0.1";
        this.port = 3306;
        this.database = "s20_proxy";
        this.username = "username";
        this.password = "password";
    }
}