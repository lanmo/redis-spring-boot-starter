package org.ifaster.redis.constant;

/**
 * @author yangnan
 */
public class RedisClientConstant {
    /**
     * 连接超时时间 ms
     */
    public static final int CONNECTION_TIMEOUT = 1000;
    /**
     * 读超时间 ms
     */
    public static final int SO_TIMEOUT = 1000;
    /**
     * 最大空闲连接数数
     */
    public static final int MAX_IDLE = 50;
    /**
     * 最大连接数
     */
    public static final int MAX_TOTAL = 1024;
    /**
     * 最小空闲连接数
     */
    public static final int MIN_IDLE = 30;
    /**
     * 链接异常重试次数
     */
    public static final int MAX_ATTEMPTS = 2;
    /**
     * 连接空闲多长时间后将被释放，timeBetweenEvictionRunsMillis > 0 才有意义
     */
    public static final long MIN_EVICT_ABLE_IDLE_TIME_MILLIS = 1000L * 60L * 30L;
    public static final long TIME_BETWEEN_EVICTION_RUNS_MILLIS = 1000L * 60L * 10;
}
