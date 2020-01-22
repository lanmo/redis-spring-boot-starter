package org.ifaster.redis.serializer;

import org.ifaster.redis.exception.SerializationException;

/**
 * 序列化和反序列化
 *
 * @author yangnan
 */
public interface Serializer<T> {

    /**
     * 序列化对象
     *
     * @param value 对象值
     * @return   byte数组
     * @throws SerializationException
     */
    byte[] serialize(T value) throws SerializationException;

    /**
     * 反序列化
     *
     * @param data byte数组
     * @param clz  反序列化的类型
     * @param <T>
     * @return
     * @throws SerializationException
     */
    <T> T deserialize(byte[] data, Class<T> clz) throws SerializationException;
}
