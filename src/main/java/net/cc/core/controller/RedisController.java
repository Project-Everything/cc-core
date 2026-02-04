package net.cc.core.controller;

import lombok.RequiredArgsConstructor;
import net.cc.core.Constants;
import net.cc.core.CorePlugin;
import net.cc.core.model.CoreSubscriber;
import net.cc.core.model.config.RedisConfig;
import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Controller class for Redis connections.
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
public final class RedisController {

    private final CorePlugin plugin;

    private RedisClient redisClient;
    private CoreSubscriber subscriber;

    // Initializes the controller
    public void initialize() {
        final ConnectionPoolConfig poolConfig = new ConnectionPoolConfig();
        final RedisConfig redisConfig = this.plugin.getConfigController().getRedisConfig();
        final DefaultJedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
                .password(redisConfig.getPassword())
                .timeoutMillis(0)
                .build();

        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);

        // Create redis client
        this.redisClient = RedisClient.builder()
                .poolConfig(poolConfig)
                .hostAndPort(redisConfig.getHost(), redisConfig.getPort())
                .clientConfig(clientConfig)
                .build();

        this.subscriber = new CoreSubscriber(this.plugin);
        this.subscribe();
    }

    // Subscribes to a list of Redis channels
    private void subscribe() {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try {
                this.redisClient.subscribe(
                        this.subscriber,
                        Constants.REDIS_CHANNEL_SYNC,
                        Constants.REDIS_CHANNEL_UPDATE,
                        Constants.REDIS_CHANNEL_BROADCAST,
                        Constants.REDIS_CHANNEL_VANISH,
                        Constants.REDIS_CHANNEL_CHAT_MESSAGE,
                        Constants.REDIS_CHANNEL_PRIVATE_MESSAGE,
                        Constants.REDIS_CHANNEL_MAIL_MESSAGE
                );
            } catch (final RuntimeException e) {
                this.plugin.getComponentLogger().error("Failed to subscribe to channels", e);
            }
        });
    }

    // Publishes a String message to a Redis channel
    public void publish(final String channel, final String message) {
        CompletableFuture.runAsync(() -> {
            try {
                this.redisClient.publish(channel, message);
            } catch (final RuntimeException e) {
                this.plugin.getComponentLogger().error("Failed to publish message '{}'", message, e);
            }
        });
    }

    // Sets a value in Redis
    public CompletableFuture<Void> set(final String key, final String value, final long expire) {
        return CompletableFuture.runAsync(() -> {
            try {
                this.redisClient.set(key, value);
                if (expire > 0) {
                    this.redisClient.expire(key, expire);
                }
            } catch (final RuntimeException e) {
                this.plugin.getComponentLogger().error("Failed to set key '{}' to value '{}'", key, value, e);
            }
        });
    }

    // Gets a value from Redis
    public CompletableFuture<String> get(final String key) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.redisClient.get(key);
            } catch (final RuntimeException e) {
                this.plugin.getComponentLogger().error("Failed to get key '{}'", key, e);
                return null;
            }
        });
    }

    // Gets a list of keys at a Redis key
    public CompletableFuture<List<String>> getKeys(final String key) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final ScanResult<String> scanResult = this.redisClient.scan("0", new ScanParams().match(key));
                return scanResult.getResult();
            } catch (final RuntimeException e) {
                this.plugin.getComponentLogger().error("Failed to scan key '{}'", key, e);
                return null;
            }
        });
    }

    // Deletes a key-value pair from Redis
    public CompletableFuture<Void> delete(final String key) {
        return CompletableFuture.runAsync(() -> {
            try {
                this.redisClient.del(key);
            } catch (final RuntimeException e) {
                this.plugin.getComponentLogger().error("Failed to delete key '{}'", key, e);
            }
        });
    }

    // Closes the connection to Redis
    public void close() {
        this.redisClient.close();
        this.subscriber.unsubscribe();
    }

}
