package org.ifaster.redis.configuration;

import lombok.Data;

import static org.apache.commons.pool2.impl.BaseObjectPoolConfig.DEFAULT_EVICTION_POLICY_CLASS_NAME;
import static org.ifaster.redis.constant.RedisClientConstant.*;

/**
 * redis 客户端属性值
 * @author yangnan
 */
@Data
public class RedisProperties {
    /**
     * 默认开启
     */
    private boolean enable = true;
    /**
     * 客户端名
     */
    private String name = "redisClient";

    /**
     * 密码
     */
    private String password;

    /**
     * 集群的key的前缀
     */
    private String prefix;

    /**
     * 集群地址 ip1:port1, ip2:port2
     */
    private String cluster;

    /**
     * 序列化
     */
    private String serializer;

    /**
     * 事件拦截器
     */
    private String eventListener;

    /**
     * 连接超时时间 ms
     */
    private int connectionTimeout = CONNECTION_TIMEOUT;
    /**
     * 读超时间 ms
     */
    private int soTimeout = SO_TIMEOUT;
    /**
     * 最大空闲连接数数
     */
    private int maxIdle = MAX_IDLE;
    /**
     * 最大连接数
     */
    private int maxTotal = MAX_TOTAL;
    /**
     * 最小空闲连接数
     */
    private int minIdle = MIN_IDLE;
    /**
     * 链接异常重试次数
     */
    private int maxAttempts = MAX_ATTEMPTS;
    /**
     * 连接空闲多长时间后将被释放，timeBetweenEvictionRunsMillis > 0 才有意义
     */
    private long minEvictableIdleTimeMillis = MIN_EVICT_ABLE_IDLE_TIME_MILLIS;
    private long timeBetweenEvictionRunsMillis = TIME_BETWEEN_EVICTION_RUNS_MILLIS;
    /**
     * 过期策略
     */
    private String evictionPolicyClassName = DEFAULT_EVICTION_POLICY_CLASS_NAME;
}
