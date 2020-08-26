package com.max.jedis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisShardInfo;

import java.util.HashMap;
import java.util.Map;

public class JedisTest {

    public static void main(String[] args) {
        //1.jedis对象
        Jedis jedis = new Jedis("192.168.177.197",6379);
        jedis.auth("123456");
        //测试连接
        System.out.println(jedis.ping());
        jedis.close();
    }

}
