# Jedis

>什么是jedis，相当于jdbc，用来操作redis

### 导入相关依赖

```xml
<!-- https://mvnrepository.com/artifact/redis.clients/jedis -->
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
    <version>3.3.0</version>
</dependency>
<!-- fastjson -->
<dependency>
	<groupId>com.alibaba</groupId>
	<artifactId>fastjson</artifactId>
	<version>1.2.73</version>
</dependency>
```

注：据别人说fastjson有漏洞，工作中不建议使用

### 编码测试

>连接数据库

```java
//1.jedis对象 
Jedis jedis = new Jedis("192.168.177.197",6379);
jedis.auth("123456");
//redis默认是不允许远程连接的，如果要在程序中远程连接，需要将配置文件中bind 127.0.0.1注释掉，将protected mode 设置为no
//这里我增加了requirepass 123456 所以需要密码
```

>进行操作

```java
//测试连接
System.out.println(jedis.ping());//返回pong 表示测试成功
```

>更多的api操作不再演示，基本和命令行中的命令一样

### 事务的使用

```java
 		//1.jedis对象
        Jedis jedis = new Jedis("192.168.177.197",6379);
        jedis.auth("123456");
        //清空数据库
        jedis.flushDB();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name","ydd");
        jsonObject.put("age", "28");
        //开启事务
        Transaction tx = jedis.multi();
        try {
            tx.set("user1", jsonObject.toJSONString());
            tx.set("user2", jsonObject.toJSONString());
            //执行事务
            tx.exec();
        } catch (Exception e) {
            e.printStackTrace();
            //放弃事务
            tx.discard();
        } finally {
            System.out.println(jedis.get("user1"));
            System.out.println(jedis.get("user2"));
            jedis.close();
        }
```

# springboot 整合redis

> 在springboot2.x之后原来的jedis被替换为了lettuce
>
> lettuce 使用netty，实例可以在多个线程间共享，不存在线程不安全的情况，可以减少线程数据了

### 如何查看springboot关于redis都有哪些配置？

> 首先找到autoconfigure.jar中的spring.factories

![](.\images\1.png )

> 打开之后搜索redis，可以看到是哪个类自动配置了redis

![image-20200826164403602](.\images\2.png)

> 打开类之后寻找springboot使用的哪个配置类来进行配置，点进配置类后就可以看到默认的属性配置和都有哪些可以配置

![image-20200826164622747](.\images\3.png)

### 编码测试

> 配置数据库连接信息

```properties
#数据库信息
spring.redis.host=192.168.177.197
spring.redis.port=6379
spring.redis.password=123456
```

> 测试代码

```java
package com.max.springbootredis;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes=SpringbootRedisApplication.class)
@RunWith(SpringRunner.class)
class SpringbootRedisApplicationTests {

    @Autowired
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
}
```

> 自定义RedisTemplate

```java
package com.max.springbootredis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.net.UnknownHostException;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) throws UnknownHostException {
        RedisTemplate<String, Object> template = new RedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);

        //配置具体的序列化方式
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new 
        Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        //普通key和hash的key的序列化
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        //普通value和hash的value的序列化
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }
}

```

### 小结

> 实际工作中我们并不需要一遍遍的写原生的api，我们可以使用一个工具类将其封装起来，以便我们使用

# redis 配置文件详解

