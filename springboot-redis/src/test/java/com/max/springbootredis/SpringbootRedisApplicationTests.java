package com.max.springbootredis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.max.springbootredis.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes=SpringbootRedisApplication.class)
@RunWith(SpringRunner.class)
class SpringbootRedisApplicationTests {

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate redisTemplate;

    @Test
    public void testRedisTemplate(){
        //操作各种类型要先opsForXXX.后面的方法和jedis差不多
        //opsForValue() 字符串
        //opsForList() list
        //opsForSet() set
        //opsForHash() hash
        //.... zset geo hyperloglog bitmap
        redisTemplate.opsForValue().set("name","max");
        //获取连接的操作
        //redisTemplate.getConnectionFactory().getConnection().flushDb();
    }

    @Test
    public void test() throws JsonProcessingException {
        User user = new User("ydd", 28);
        //String json = new ObjectMapper().writeValueAsString(user);
        redisTemplate.opsForValue().set("user", user);
        System.out.println(redisTemplate.opsForValue().get("user"));
    }

}
