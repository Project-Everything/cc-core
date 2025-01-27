package net.cc.core.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public final class DatabaseSettings {
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    // Constructor
    public DatabaseSettings() {
        this.host = "localhost";
        this.port = 3306;
        this.database = "database";
        this.username = "username";
        this.password = "";
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
