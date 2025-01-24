package net.cc.core.storage;

import net.cc.core.CorePlugin;
import org.bukkit.configuration.file.FileConfiguration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.List;

public final class RedisManager {

    private final CorePlugin plugin;
    private JedisPool pool;

    public RedisManager(final CorePlugin plugin) {
        this.plugin = plugin;

        init();
    }

    private void init() {
        final FileConfiguration config = plugin.getConfig();
        final String host = config.getString("redis.host");
        final int port = config.getInt("redis.port");
        final String password = config.getString("redis.password");

        final JedisPoolConfig poolConfig = new JedisPoolConfig();

        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);

        pool = new JedisPool(poolConfig, host, port, 0, password);
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
