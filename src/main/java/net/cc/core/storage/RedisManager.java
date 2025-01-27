package net.cc.core.storage;

import net.cc.core.CorePlugin;
import net.cc.core.config.ConfigManager;
import net.cc.core.config.RedisSettings;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.List;

public final class RedisManager {

    private final CorePlugin plugin;
    private final ConfigManager config;
    private JedisPool pool;

    public RedisManager(final CorePlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();

        init();
    }

    private void init() {
        final RedisSettings settings = config.getRedisSettings();
        final JedisPoolConfig poolConfig = new JedisPoolConfig();

        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);

        pool = new JedisPool(poolConfig, settings.getHost(), settings.getPort(), 0, settings.getPassword());
    }

    public void set(final String key, final String value, final int expire) {
        try (Jedis jedis = pool.getResource()) {
            jedis.set(key, value);
            if (expire > 0) {
                jedis.expire(key, expire);
            }
        }
    }

    public String get(final String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.get(key);
        }
    }

    public List<String> getValues(final String key) {
        try (Jedis jedis = pool.getResource()) {
            final ScanResult<String> scanResult = jedis.scan("0", new ScanParams().match(key));
            return scanResult.getResult();
        }
    }

    public void close() {
        pool.close();
    }
}
