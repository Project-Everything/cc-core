package net.cc.core.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public final class RedisSettings {
    private final String host;
    private final int port;
    private final String password;

    // Constructor
    public RedisSettings() {
        this.host = "localhost";
        this.port = 3306;
        this.password = "";
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }
}
