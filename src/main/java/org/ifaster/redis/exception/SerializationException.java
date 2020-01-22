package org.ifaster.redis.exception;

/**
 * 异常
 *
 * @author yangnan
 */
public class SerializationException extends RuntimeException {
    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
