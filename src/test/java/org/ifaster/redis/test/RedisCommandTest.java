package org.ifaster.redis.test;

import org.ifaster.redis.client.RedisClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
//@ContextConfiguration(classes = SpringBootStarter.class)
public class RedisCommandTest {

    @Autowired
    private RedisClient client;

    @Test
    public void testSet() {
        String key = "test";
//        client.set(key, 10, "1111");
        String abc = client.get(key, String.class);
        System.out.println(abc);
        Assert.assertEquals("1111", abc);
    }

    @Test
    public void testHget() {
        String key = "testHash";
        String abc = client.hget(key, "fff", String.class);
        System.out.println(abc);
        Assert.assertEquals("1111", abc);
    }

    @Test
    public void testHgetAll() {
        String key = "testHash";
        Map<String, Object > map = client.hgetAll(key, Object.class);
        System.out.println(map);
    }

    @Test
    public void testHset() {
        String key = "testHash";
        long abc = client.hset(key, "fff", new SerializerTest.Person());
        System.out.println(abc);
        Assert.assertEquals("1111", abc);
    }

    @Test
    public void testDel() {
        String key = "testHash";
        long a = client.hdel(key, "fff");
        System.out.println(a);
    }
}
