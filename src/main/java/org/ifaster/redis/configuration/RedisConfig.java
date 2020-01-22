package org.ifaster.redis.configuration;

import lombok.Data;

import java.util.List;

/**
 * @author yangnan
 */
@Data
public class RedisConfig {
    private List<RedisProperties> clusters;
}
