package org.ifaster.redis.client.support;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.ifaster.redis.client.RedisClient;
import org.ifaster.redis.configuration.RedisProperties;
import org.ifaster.redis.event.RedisCommandEvent;
import org.ifaster.redis.event.RedisEvent;
import org.ifaster.redis.exception.RedisConfigException;
import org.ifaster.redis.exception.SerializationException;
import org.ifaster.redis.listener.DefaultRedisEventListener;
import org.ifaster.redis.listener.RedisEventListener;
import org.ifaster.redis.serializer.FastJsonSerializer;
import org.ifaster.redis.serializer.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.params.SetParams;

import java.util.*;

import static org.apache.commons.pool2.impl.BaseObjectPoolConfig.DEFAULT_EVICTION_POLICY_CLASS_NAME;
import static org.ifaster.redis.constant.RedisClientConstant.*;

/**
 * @author yangnan
 */
public class RedisClientSupport implements RedisClient {

    private static Logger LOGGER = LoggerFactory.getLogger(RedisClientSupport.class);

    private JedisCluster jedisCluster;
    private Serializer serializer;
    private RedisEventListener eventListener;
    private String cluster;

    /**
     * redis key前缀
     */
    private String prefix;

    /**
     * 构造函数
     *
     * @param redisProperties
     */
    public RedisClientSupport(RedisProperties redisProperties, Serializer serializer, RedisEventListener eventListener) {
        this(redisProperties.getPrefix(), redisProperties.getPassword()
                ,redisProperties.getCluster(), serializer
                ,redisProperties.getConnectionTimeout(), redisProperties.getSoTimeout()
                ,redisProperties.getMaxIdle(), redisProperties.getMaxTotal()
                ,redisProperties.getMinIdle(), redisProperties.getMaxAttempts()
                ,redisProperties.getMinEvictableIdleTimeMillis()
                ,redisProperties.getTimeBetweenEvictionRunsMillis()
                ,redisProperties.getEvictionPolicyClassName(),
                eventListener);
    }

    /**
     * 构造函数
     *
     * @param redisProperties
     */
    public RedisClientSupport(RedisProperties redisProperties) {
        this(redisProperties, null, null);
    }

    /**
     * 构造函数
     *
     * @param prefix
     * @param cluster
     * @param password
     * @param eventListener
     */
    public RedisClientSupport(String prefix, String password, String cluster, RedisEventListener eventListener) {
        this(prefix, password, cluster, null, eventListener);
    }

    /**
     * 构造函数
     *
     * @param prefix
     * @param cluster
     * @param password
     * @param serializer
     */
    public RedisClientSupport(String prefix, String password, String cluster, Serializer serializer) {
        this(prefix, password, cluster, serializer, null);
    }

    /**
     * 构造函数
     *
     * @param prefix
     * @param cluster
     * @param password
     * @param serializer
     * @param eventListener
     */
    public RedisClientSupport(String prefix, String password, String cluster, Serializer serializer, RedisEventListener eventListener) {
        this(prefix, password, cluster, serializer, CONNECTION_TIMEOUT, SO_TIMEOUT, MAX_IDLE
                ,MAX_TOTAL, MIN_IDLE, MAX_ATTEMPTS, MIN_EVICT_ABLE_IDLE_TIME_MILLIS, TIME_BETWEEN_EVICTION_RUNS_MILLIS,
                DEFAULT_EVICTION_POLICY_CLASS_NAME, eventListener);
    }

    /**
     * 构造函数
     *
     * @param prefix
     * @param cluster
     * @param password
     */
    public RedisClientSupport(String prefix, String password, String cluster) {
        this(prefix, password, cluster, null, null);
    }

    /**
     * @param prefix                        前缀
     * @param password                      密码
     * @param cluster                       集群ip ip1:port1,ip2:port2
     * @param serializer                    序列化
     * @param connectionTimeout             连接超时时间 ms
     * @param soTimeout                     读超时间 ms
     * @param maxIdle                       最大空闲连接数数
     * @param maxTotal                      最大连接数
     * @param minIdle                       最小空闲连接数
     * @param maxAttempts                   链接异常重试次数
     * @param minEvictableIdleTimeMillis    连接的最小空闲时间多长时间后将被释放，timeBetweenEvictionRunsMillis > 0 才有意义
     * @param timeBetweenEvictionRunsMillis 空闲时间连接检测周期
     * @param evictionPolicyClassName       过期策略
     * @param eventListener                 拦截器类
     */
    public RedisClientSupport(String prefix, String password, String cluster,
                              Serializer serializer, int connectionTimeout,
                              int soTimeout, int maxIdle, int maxTotal,
                              int minIdle, int maxAttempts, long minEvictableIdleTimeMillis,
                              long timeBetweenEvictionRunsMillis, String evictionPolicyClassName,
                              RedisEventListener eventListener) {
        if (StringUtils.isEmpty(cluster)) {
            throw new RedisConfigException("cluster is null");
        }
        this.cluster = cluster;
        this.prefix = StringUtils.isEmpty(prefix) ? "" : prefix;
        if (serializer == null) {
            this.serializer = new FastJsonSerializer();
        } else {
            this.serializer = serializer;
        }
        if (eventListener == null) {
            this.eventListener = new DefaultRedisEventListener();
        } else {
            this.eventListener = eventListener;
        }
        String[] clusters = cluster.split(",");
        Set<HostAndPort> jedisCluster = new HashSet<>();
        for (String c : clusters) {
            String[] cs = c.split(":");
            jedisCluster.add(new HostAndPort(cs[0], Integer.parseInt(cs[1])));
        }

        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setEvictionPolicyClassName(evictionPolicyClassName);
        poolConfig.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        poolConfig.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);

        this.jedisCluster = new JedisCluster(jedisCluster, connectionTimeout, soTimeout, maxAttempts, password, poolConfig);
    }

    /**
     * key 序列化
     *
     * @param key
     * @return
     */
    private byte[] getKey(String key) {
        if (StringUtils.isEmpty(key)) {
            throw new SerializationException("redis key must be not null");
        }
        return (prefix + key).getBytes(UTF_8);
    }

    /**
     * field 序列化
     *
     * @param field
     * @return
     */
    private byte[] getBytes(String field) {
        if (StringUtils.isEmpty(field)) {
            throw new SerializationException("redis hash field must be not null");
        }
        return field.getBytes(UTF_8);
    }

    /**
     * 获取原始keys
     *
     * @param keys
     * @return
     */
    private String getOriginalKey(byte[] keys) {
        if (keys == null || keys.length < 1) {
            throw new SerializationException("redis byte's key must be not null");
        }
        return new String(keys, UTF_8).substring(prefix.length() + 1);
    }

    /**
     * 序列化map
     *
     * @param map
     * @return
     */
    private Map<byte[], byte[]> serializerMap(Map<String, ?> map) {
        if (map == null || map.isEmpty()) {
            return new HashMap<>(0);
        }
        Map<byte[], byte[]> res = new HashMap<>(map.size());
        map.forEach((k, v) -> res.put(getBytes(k), serializer.serialize(v)));
        return res;
    }

    /**
     * 创建事件
     *
     * @param key
     * @param command
     * @return
     */
    private RedisEvent create(String key, String command) {
        return RedisCommandEvent.builder().cluster(cluster).command(command).key(key).commandStart(System.currentTimeMillis()).build();
    }

    /**
     * 创建事件
     *
     * @param keys
     * @param command
     * @return
     */
    private RedisEvent create(String command, String... keys) {
        return RedisCommandEvent.builder().cluster(cluster).command(command).keys(keys).commandStart(System.currentTimeMillis()).build();
    }

    @Override
    public String set(String key, int expTime, Object value) {
        RedisEvent event = create(key, "setex");
        try {
            eventListener.before(event);
            return jedisCluster.setex(getKey(key), expTime, serializer.serialize(value));
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        return null;
    }

    @Override
    public String set(String key, Object value, SetParams params) {
        RedisEvent event = create(key, "set");
        try {
            eventListener.before(event);
            return jedisCluster.set(getKey(key), serializer.serialize(value), params);
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        return null;
    }

    @Override
    public <T> T get(String key, Class<T> classType) {
        RedisEvent event = create(key, "get");
        try {
            eventListener.before(event);
            return (T) serializer.deserialize(jedisCluster.get(getKey(key)), classType);
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        return null;
    }

    @Override
    public long delete(String... keys) {
        RedisEvent event = create("del", keys);
        try {
            eventListener.before(event);
            byte[][] keyBytes = new byte[keys.length][];
            for (int i = 0; i < keys.length; i++) {
                keyBytes[i] = getKey(keys[i]);
            }
            Long c = jedisCluster.del(keyBytes);
            return c == null ? 0L : c;
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        return 0L;
    }

    @Override
    public long hincrBy(String key, String field, long value) {
        RedisEvent event = create(key, "hincrBy");
        try {
            eventListener.before(event);
            Long c = jedisCluster.hincrBy(getKey(key), getBytes(field), value);
            return c == null ? 0L : c;
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        return 0L;
    }

    @Override
    public long incrBy(String key, long delta) {
        RedisEvent event = create(key, "incrBy");
        try {
            eventListener.before(event);
            Long c = jedisCluster.incrBy(getKey(key), delta);
            return c == null ? 0L : c;
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        return 0L;
    }

    @Override
    public long rpush(String key, Object value) {
        RedisEvent event = create(key, "rpush");
        try {
            eventListener.before(event);
            Long c = jedisCluster.rpush(getKey(key), serializer.serialize(value));
            return c == null ? 0L : c;
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        return 0L;
    }

    @Override
    public <T> T lpop(String key, Class<T> javaType) {
        RedisEvent event = create(key, "lpop");
        try {
            eventListener.before(event);
            return (T) serializer.deserialize(jedisCluster.lpop(getKey(key)), javaType);
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        return null;
    }

    @Override
    public <T> List<T> blpop(String key, int timeout, Class<T> javaType) {
        RedisEvent event = create(key, "blpop");
        List<byte[]> data = null;
        try {
            eventListener.before(event);
            data = jedisCluster.blpop(timeout, getKey(key));
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        if (data == null || data.size() < 1) {
            return null;
        }
        List<T> rs = new ArrayList<>(data.size());
        data.forEach(r -> rs.add((T) serializer.deserialize(r, javaType)));
        data.clear();
        data = null;
        return rs;
    }

    @Override
    public <T> List<T> lrange(String key, int start, int end, Class<T> javaType) {
        RedisEvent event = create(key, "lrange");
        List<byte[]> data = null;
        try {
            eventListener.before(event);
            data = jedisCluster.lrange(getKey(key), start, end);
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        if (data == null || data.size() < 1) {
            return new ArrayList<>(0);
        }
        List<T> rs = new ArrayList<>(data.size());
        data.forEach(r -> rs.add((T) serializer.deserialize(r, javaType)));
        data.clear();
        data = null;
        return rs;
    }

    @Override
    public long expire(String key, int seconds) {
        RedisEvent event = create(key, "expire");
        try {
            eventListener.before(event);
            Long c = jedisCluster.expire(getKey(key), seconds);
            return c == null ? 0L : c;
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        return 0L;
    }

    @Override
    public long expireAt(String key, int unixTime) {
        RedisEvent event = create(key, "expireAt");
        try {
            eventListener.before(event);
            Long c = jedisCluster.expireAt(getKey(key), unixTime);
            return c == null ? 0L : c;
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        return 0L;
    }

    @Override
    public boolean exists(String key) {
        RedisEvent event = create(key, "exists");
        try {
            eventListener.before(event);
            Boolean exists = jedisCluster.exists(getKey(key));
            return exists == null ? false : exists;
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        return false;
    }

    @Override
    public long hdel(String key, String... field) {
        if (field == null || field.length < 1) {
            return 0L;
        }
        byte[][] data = new byte[field.length][];
        for (int i = 0; i < field.length; i++) {
            data[i] = getBytes(field[i]);
        }
        RedisEvent event = create(key, "hdel");
        try {
            eventListener.before(event);
            Long c =  jedisCluster.hdel(getKey(key), data);
            return c == null ? 0 : c;
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            for (int i = 0; i < field.length; i++) {
                data[i] = null;
            }
            data = null;
            eventListener.complete(event);
        }
        return 0L;
    }

    @Override
    public String hmset(String key, Map<String, ?> map) {
        RedisEvent event = create(key, "hmset");
        try {
            eventListener.before(event);
            return jedisCluster.hmset(getKey(key), serializerMap(map));
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        return null;
    }

    @Override
    public <T> Set<T> smembers(String key, Class<T> javaType) {
        RedisEvent event = create(key, "smembers");
        Set<byte[]> data = null;
        try {
            eventListener.before(event);
            data = jedisCluster.smembers(getKey(key));
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        if (data == null || data.isEmpty()) {
            return new HashSet<>();
        }
        Set<T> rs = new HashSet<>(data.size());
        data.forEach(r -> rs.add((T) serializer.deserialize(r, javaType)));
        data.clear();
        data = null;
        return rs;
    }

    @Override
    public <T> T getSet(String key, Object value, Class<T> javaType) {
        RedisEvent event = create(key, "getSet");
        byte[] data = null;
        try {
            eventListener.before(event);
            data = jedisCluster.getSet(getKey(key), serializer.serialize(value));
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        return (T) serializer.deserialize(data, javaType);
    }

    @Override
    public long sadd(String key, Object... values) {
        if (values == null || values.length < 1) {
            return 0L;
        }
        byte[][] data = new byte[values.length][];
        for (int i=0; i<data.length; i++) {
            data[i] = serializer.serialize(values[i]);
        }
        RedisEvent event = create(key, "sadd");
        try {
            eventListener.before(event);
            Long c = jedisCluster.sadd(getKey(key), data);
            return c == null ? 0L : c;
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            for (int i=0; i<data.length; i++) {
                data[i] = null;
            }
            data = null;
            eventListener.complete(event);
        }
        return 0L;
    }

    @Override
    public long srem(String key, Object... values) {
        if (values == null || values.length < 1) {
            return 0L;
        }
        byte[][] data = new byte[values.length][];
        for (int i=0; i<data.length; i++) {
            data[i] = serializer.serialize(values[i]);
        }
        RedisEvent event = create(key, "srem");
        try {
            eventListener.before(event);
            Long c = jedisCluster.srem(getKey(key), data);
            return c == null ? 0L:c;
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            for (int i=0; i<data.length; i++) {
                data[i] = null;
            }
            data = null;
            eventListener.complete(event);
        }
        return 0L;
    }

    @Override
    public long llen(String key) {
        RedisEvent event = create(key, "llen");
        try {
            eventListener.before(event);
            Long c = jedisCluster.llen(getKey(key));
            return c == null ? 0L:c;
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        return 0L;
    }

    @Override
    public long lrem(String key, long count, Object value) {
        RedisEvent event = create(key, "lrem");
        try {
            eventListener.before(event);
            Long c = jedisCluster.lrem(getKey(key), count, serializer.serialize(value));
            return c == null ? 0L:c;
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        return 0L;
    }

    @Override
    public long scard(String key) {
        RedisEvent event = create(key, "scard");
        try {
            eventListener.before(event);
            Long c = jedisCluster.scard(getKey(key));
            return c == null ? 0L:c;
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        return 0L;
    }

    @Override
    public boolean sismember(String key, Object member) {
        RedisEvent event = create(key, "sismember");
        try {
            eventListener.before(event);
            Boolean es = jedisCluster.sismember(getKey(key), serializer.serialize(member));
            return  es == null ? false : es;
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        return false;
    }

    @Override
    public long zadd(String key, Object member, double score) {
        RedisEvent event = create(key, "zadd");
        try {
            eventListener.before(event);
            Long c = jedisCluster.zadd(getKey(key), score, serializer.serialize(member));
            return c == null ? 0L:c;
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        return 0L;
    }

    @Override
    public long zadd(String key, Map<Object, Double> scoreMembers) {
        if (scoreMembers == null || scoreMembers.isEmpty()) {
            return 0L;
        }
        Map<byte[], Double> data = new HashMap<>(scoreMembers.size());
        scoreMembers.forEach((k, v) -> data.put(serializer.serialize(k), v));
        RedisEvent event = create(key, "zadd");
        try {
            eventListener.before(event);
            Long c = jedisCluster.zadd(getKey(key), data);
            return c == null ? 0L:c;
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        return 0L;
    }

    @Override
    public long zcard(String key) {
        RedisEvent event = create(key, "zcard");
        try {
            eventListener.before(event);
            Long c = jedisCluster.zcard(getKey(key));
            return c == null ? 0L:c;
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        return 0L;
    }

    @Override
    public long zcount(String key, double min, double max) {
        RedisEvent event = create(key, "zcount");
        try {
            eventListener.before(event);
            Long c = jedisCluster.zcount(getKey(key), min, max);
            return c == null ? 0L:c;
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        return 0L;
    }

    @Override
    public long zrem(String key, Object... member) {
        if (member == null || member .length < 1) {
            return 0L;
        }
        byte[][] data = new byte[member.length][];
        for (int i=0 ;i<member.length; i++) {
            data[i] = serializer.serialize(member);
        }
        RedisEvent event = create(key, "zrem");
        try {
            eventListener.before(event);
            Long c = jedisCluster.zrem(getKey(key), data);
            return c == null ? 0L:c;
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            for (int i=0 ;i<member.length; i++) {
                data[i] = null;
            }
            data = null;
            eventListener.complete(event);
        }
        return 0L;
    }

    @Override
    public long hset(String key, String field, Object value) {
        RedisEvent event = create(key, "hset");
        try {
            eventListener.before(event);
            Long c = jedisCluster.hset(getKey(key), getBytes(field), serializer.serialize(value));
            return c == null ? 0L:c;
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        return 0L;
    }

    @Override
    public <T> T hget(String key, String field, Class<T> valueType) {
        RedisEvent event = create(key, "hget");
        byte[] data = null;
        try {
            eventListener.before(event);
            data = jedisCluster.hget(getKey(key), getBytes(field));
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        return (T) serializer.deserialize(data, valueType);
    }

    @Override
    public <T> Map<String, T> hgetAll(String key, Class<T> valueType) {
        RedisEvent event = create(key, "hgetAll");
        Map<byte[], byte[]> map = null;
        try {
            eventListener.before(event);
            map = jedisCluster.hgetAll(getKey(key));
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        if (map == null || map.isEmpty()) {
            return null;
        }
        Map<String, T> result = new HashMap<>(map.size());
        map.forEach((k, v) -> result.put(new String(k, UTF_8), (T) serializer.deserialize(v, valueType)));
        map.clear();
        map = null;
        return result;
    }

    @Override
    public long hlen(String key) {
        RedisEvent event = create(key, "hlen");
        try {
            eventListener.before(event);
            Long c = jedisCluster.hlen(getKey(key));
            return c == null ? 0L:c;
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        return 0L;
    }

    @Override
    public <T> List<T> hmget(String key, Class<T> valueType, String... field) {
        if (field == null || field.length < 1) {
            return new ArrayList<>(0);
        }
        byte[][] fieldBytes = new byte[field.length][];
        for (int i=0; i<field.length; i++) {
            fieldBytes[i] = getBytes(field[i]);
        }
        RedisEvent event = create(key, "hmget");
        List<byte[]> data = null;
        try {
            eventListener.before(event);
            data = jedisCluster.hmget(getKey(key), fieldBytes);
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        if (data == null || data.isEmpty()) {
            return new ArrayList<>(1);
        }

        List<T> rs = new ArrayList<>(data.size());
        data.forEach( r -> rs.add((T) serializer.deserialize(r, valueType)));
        data.clear();
        data = null;
        return rs;
    }

    @Override
    public long hsetnx(String key, String field, Object value) {
        RedisEvent event = create(key, "hsetnx");
        try {
            eventListener.before(event);
            Long c = jedisCluster.hsetnx(getKey(key), getBytes(field), serializer.serialize(value));
            return c == null ? 0L:c;
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        return 0L;
    }

    @Override
    public <T> List<T> zrange(String key, long start, long end, Class<T> valueType) {
        RedisEvent event = create(key, "zrange");
        Set<byte[]> data = null;
        try {
            eventListener.before(event);
            data = jedisCluster.zrange(getKey(key), start, end);
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        if (data == null || data.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<T> rs = new ArrayList<>(data.size());
        data.forEach( r -> rs.add((T) serializer.deserialize(r, valueType)));
        data.clear();
        data = null;
        return rs;
    }

    @Override
    public <T> List<T> zrevrange(String key, long start, long end, Class<T> valueType) {
        RedisEvent event = create(key, "zrevrange");
        Set<byte[]> data = null;
        try {
            eventListener.before(event);
            data = jedisCluster.zrevrange(getKey(key), start, end);
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        if (data == null || data.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<T> rs = new ArrayList<>(data.size());
        data.forEach( r -> rs.add((T) serializer.deserialize(r, valueType)));
        data.clear();
        data = null;
        return rs;
    }

    @Override
    public long zrevrank(String key, Object member) {
        RedisEvent event = create(key, "zrevrank");
        try {
            eventListener.before(event);
            Long c = jedisCluster.zrevrank(getKey(key), serializer.serialize(member));
            return c == null ? 0L:c;
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        return 0L;
    }

    @Override
    public long zrank(String key, Object member) {
        RedisEvent event = create(key, "zrank");
        try {
            eventListener.before(event);
            Long c = jedisCluster.zrank(getKey(key), serializer.serialize(member));
            return c == null ? 0L:c;
        } catch (Throwable e) {
            eventListener.error(event, e);
        } finally {
            eventListener.complete(event);
        }
        return 0L;
    }
}
