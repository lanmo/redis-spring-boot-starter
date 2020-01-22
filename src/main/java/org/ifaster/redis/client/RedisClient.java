package org.ifaster.redis.client;

import redis.clients.jedis.params.SetParams;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * redis客户端操作命令
 *
 * @author yangnan
 */
public interface RedisClient {

    /**
     * 字符集
     */
    Charset UTF_8 = Charset.forName("UTF-8");

    /**
     * 设置缓存对象
     *
     * @param key
     * @param value
     * @param expTime 单位(秒)
     * @return
     */
    String set(final String key, final int expTime, final Object value);

    /**
     *
     * @param key
     * @param value
     * @param params
     * @return
     */
    String set(final String key, final Object value, SetParams params);

    /**
     * 获取缓存对象，不支持list，map
     *
     * @param key
     * @param classType
     * @param <T>
     * @return
     */
    <T> T get(String key, Class<T> classType);

    /**
     * 删除缓存对象
     *
     * @param keys
     * @return
     */
    long delete(final String... keys);

    /**
     * 为哈希表 key 中的域 field 的值加上增量 increment
     *
     * @param key
     * @param field
     * @param increment
     * @return
     */
    long hincrBy(final String key, final String field, final long increment);

    /**
     * @param key
     * @param delta 累加值，非负值
     * @return
     */
    long incrBy(final String key, final long delta);

    /**
     *
     * @param key
     * @param value
     * @return
     */
    long rpush(final String key, final Object value);

    /**
     *
     * @param key
     * @param javaType
     * @param <T>
     * @return
     */
    <T> T lpop(final String key, Class<T> javaType);

    /**
     *
     * @param key
     * @param timeout
     * @param javaType
     * @param <T>
     * @return
     */
    <T> List<T> blpop(final String key, final int timeout, Class<T> javaType);

    /** 返回存储在 key 的列表里指定范围内的元素。*/
    <T> List<T> lrange(final String key, int start, int end, Class<T> javaType);

    /**
     *
     * @param key
     * @param seconds
     * @return
     */
    long expire(final String key, final int seconds);

    /**
     *
     * @param key
     * @param unixTime
     * @return
     */
    long expireAt(String key, int unixTime);

    /**
     *
     * @param key
     * @return
     */
    boolean exists(final String key);

    /** 删除哈希表 key 中的一个或多个指定域，不存在的域将被忽略。*/
    long hdel(String key, String... field);

    /** 同时将多个 field-value (域-值)对设置到哈希表 key 中 */
    String hmset(String key, Map<String, ?> map);

    /**
     *
     * @param key
     * @param value
     * @param javaType
     * @param <T>
     * @return
     */
    <T> T getSet(final String key, final Object value, Class<T> javaType);

    /**
     * 返回存储在 key 里的list的长度。
     * 如果 key 不存在，那么就被看作是空list，并且返回长度为 0。
     * @param key
     * @return
     */
    long llen(String key);

    /**
     * 从存于 key 的列表里移除前 count 次出现的值为 value 的元素。
     * 如果list里没有存在key就会被当作空list处理，所以当 key 不存在的时候，这个命令会返回 0。
     * @param key
     * @param count
     * @param value
     * @return
     */
    long lrem(String key, long count, Object value);

    /**
     * 将一个或多个 member 元素加入到集合 key 当中，已经存在于集合的 member 元素将被忽略。
     * 假如 key 不存在，则创建一个只包含 member 元素作成员的集合。
     * 当 key 不是集合类型时，返回一个错误。
     */
    long sadd(String key, Object ... values);

    /**
     * 集合的基数(元素的数量),如果key不存在,则返回 0.
     * @param key
     * @return
     */
    long scard(String key);

    /**
     * 返回成员 member 是否是存储的集合 key的成员.
     * @param key
     * @param member
     * @return
     */
    boolean sismember(String key, Object member);

    /**
     *返回集合 key 中的所有成员。不存在的 key 被视为空集合。
     * @param key
     * @param valueType
     * @param <T>
     * @return
     */
    <T> Set<T> smembers(String key, Class<T> valueType);

    /**
     * 移除集合 key 中的一个或多个 member 元素，不存在的 member 元素会被忽略。
     * @param key
     * @param values
     * @return
     */
    long srem(String key, Object... values);

    /**
     * 将一个member 元素及其 score 值加入到有序集 key 当中。
     * @param key
     * @param member
     * @param score
     * @return
     */
    long zadd(String key, Object member, double score);

    /**
     * 将一个或多个 member 元素及其 score 值加入到有序集 key 当中。
     * @param key
     * @param scoreMembers
     * @return
     */
    long zadd(String key,  Map<Object, Double> scoreMembers);

    /**
     * 返回key的有序集元素个数。
     * @param key
     * @return
     */
    long zcard(String key);

    /**
     * 返回有序集key中，score值在min和max之间(默认包括score值等于min或max)的成员。
     * @param key
     * @param min
     * @param max
     * @return
     */
    long zcount(String key, double min, double max);

    /**
     * 移除有序集 key 中的一个或多个成员，不存在的成员将被忽略。
     * @param key
     * @param member
     * @return
     */
    long zrem(String key, Object... member);

    /**
     * 设置 key 指定的哈希集中指定字段的值。
     * 如果 key 指定的哈希集不存在，会创建一个新的哈希集并与 key 关联。
     * 如果字段在哈希集中存在，它将被重写。
     * @param key
     * @param field
     * @param value
     * @return
     */
    long hset(String key, String field, Object value);

    /**
     * 返回哈希表 key 中给定域 field 的值。
     * @param key
     * @param field
     * @param valueType
     * @param <T>
     * @return
     */
    <T> T hget(String key, String field, Class<T> valueType);

    /**
     * 返回 key 指定的哈希集中所有的字段和值。
     * @param key
     * @param valueType
     * @param <T>
     * @return
     */
    <T> Map<String, T> hgetAll(String key, Class<T> valueType);

    /**
     * 返回 key 指定的哈希集包含的字段的数量。
     * @param key
     * @return
     */
    long hlen(String key);

    /**
     * 返回哈希表 key 中，一个或多个给定域的值。
     * @param key
     * @param valueType 值的类型
     * @param field
     * @param <T>
     * @return
     */
    <T> List<T> hmget(String key, Class<T> valueType, String... field);

    /**
     * 将哈希表 key 中的域 field 的值设置为 value ，当且仅当域 field 不存在。若域 field 已经存在，该操作无效。
     * 设置成功，返回 1 。
     * 如果给定域已经存在且没有操作被执行，返回 0 。
     * @param key
     * @param field
     * @param value
     * @return
     */
    long hsetnx(String key, String field, Object value);

    /**
     * 返回有序集 key 中，指定区间内的成员。
     * 其中成员的位置按 score 值递增(从小到大)来排序。
     * @param key
     * @param start
     * @param end
     * @param valueType
     * @param <T>
     * @return
     */
    <T> List<T> zrange(String key, long start, long end, Class<T> valueType);

    /**
     * 返回有序集key中，指定区间内的成员。
     * 其中成员的位置按score值递减(从大到小)来排列。
     * 具有相同score值的成员按字典序的反序排列。
     * @param key
     * @param start
     * @param end
     * @param valueType
     * @param <T>
     * @return
     */
    <T> List<T> zrevrange(String key, long start, long end, Class<T> valueType);

    /**
     * 返回有序集key中成员member的排名，
     * 其中有序集成员按score值从大到小排列。排名以0为底。
     * @param key
     * @param member
     * @return
     */
    long zrevrank(String key, Object member);

    /**
     * 返回有序集key中成员member的排名。
     * 其中有序集成员按score值递增(从小到大)顺序排列。排名以0为底。
     * @param key
     * @param member
     * @return
     */
    long zrank(String key, Object member);
}
