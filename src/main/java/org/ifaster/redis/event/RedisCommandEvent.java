package org.ifaster.redis.event;

import lombok.Builder;
import lombok.Data;

/**
 * @author yangnan
 */
@Data
@Builder
public class RedisCommandEvent implements RedisEvent {
    /**
     * redis命令
     */
    private String command;
    /**
     * 命令开始执行时间 ms
     */
    private long commandStart;
    /**
     * 集群ip
     */
    private String cluster;
    /**
     * redis的keys
     */
    private String[] keys;
    /**
     * redis的key
     */
    private String key;
}
