package org.ifaster.redis.exception;

/**
 * @author yangnan
 */
public class RedisConfigException extends RuntimeException {
    public RedisConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedisConfigException(String message) {
        super(message);
    }
}
