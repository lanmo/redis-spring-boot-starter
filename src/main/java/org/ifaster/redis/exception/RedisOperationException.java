package org.ifaster.redis.exception;

/**
 * @author yangnan
 */
public class RedisOperationException extends RuntimeException {
    public RedisOperationException(Throwable cause) {
        super(cause);
    }

    public RedisOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
