package com.max.jedis;

import com.alibaba.fastjson.JSONObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

public class JedisMulti {
    public static void main(String[] args) {
        //1.jedis对象
        Jedis jedis = new Jedis("192.168.177.197",6379);
        jedis.auth("123456");
        //清空数据库
        //jedis.flushDB();
        jedis.set("money", "1000");
        //jedis.watch("money");
        //开启事务
        Transaction tx = jedis.multi();
        try {
            tx.set("money", "200");
            //执行事务
            tx.exec();
        } catch (Exception e) {
            e.printStackTrace();
            //放弃事务
            tx.discard();
        } finally {
            System.out.println(jedis.get("money"));
            jedis.close();
        }
    }
}
