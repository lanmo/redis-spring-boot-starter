package org.ifaster.redis.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.ifaster.redis.exception.SerializationException;

import static com.alibaba.fastjson.serializer.SerializerFeature.DisableCircularReferenceDetect;

/**
 * fast json序列化
 *
 * @author yangnan
 */
public class FastJsonSerializer implements Serializer<Object> {

    private static final byte[] EMPTY = new byte[0];
    private static final SerializerFeature[] features = {DisableCircularReferenceDetect};

    @Override
    public byte[] serialize(Object value) throws SerializationException {
        if (value == null) {
            return EMPTY;
        }
        return JSON.toJSONBytes(value, features);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clz) throws SerializationException {
        if (data == null) {
            return null;
        }
        return JSON.parseObject(data, clz);
    }
}
