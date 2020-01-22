package org.ifaster.redis.test;

import lombok.Data;
import org.ifaster.redis.serializer.FastJsonSerializer;
import org.ifaster.redis.serializer.Serializer;
import org.junit.Assert;
import org.junit.Test;

/**
 * 序列化测试
 */
public class SerializerTest {

    private Serializer serializer = new FastJsonSerializer();

    @Test
    public void testSerializer() {
        Person person = new Person();
        byte[] data = serializer.serialize(person);
        Person p = (Person) serializer.deserialize(data, Person.class);
        System.out.println(p);
        Assert.assertEquals(person, p);
    }

    @Data
    static class Person {
        private String name = "test";
        private String phone = "111111111";
    }
}
