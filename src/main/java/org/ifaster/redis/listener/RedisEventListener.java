package org.ifaster.redis.listener;

import org.ifaster.redis.event.RedisEvent;
import org.ifaster.redis.exception.RedisOperationException;

import java.util.EventListener;

/**
 * 事件监听器
 *
 * @author yangnan
 */
public interface RedisEventListener extends EventListener {
    /**
     * redis命令执行前触发
     * @param event
     */
    default void before(RedisEvent event) {
    }

    /**
     * 异常拦截
     * @param event
     * @param e
     * @throws
     */
    default void error(RedisEvent event, Throwable e) {
        //默认抛出异常
        throw new RedisOperationException(e.getMessage(), e);
    }

    /**
     * redis命令完成后触发
     * @param event
     */
    default void complete(RedisEvent event) {
    }
}
