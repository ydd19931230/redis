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
//redis默认是不允许远程连接的，如果要在程序中远程连接，需要将配置文件中bind *或bind 想访问redis的地址
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

![](./images/1.png)

> 打开之后搜索redis，可以看到是哪个类自动配置了redis

![](./images/2.png)

> 打开类之后寻找springboot使用的哪个配置类来进行配置，点进配置类后就可以看到默认的属性配置和都有哪些可以配置

![](./images/3.png)

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

```properties
# units are case insensitive so 1GB 1Gb 1gB are all the same.配置文件中的单位是不区分大小写的
################################## INCLUDES ###################################
# include /path/to/local.conf
# include /path/to/other.conf
# 一个redis配置文件中可以包含多个配置文件进行组合，类似spring配置文件中的包含
################################## NETWORK #####################################
# bind 127.0.0.1 允许访问的ip，如果要可以进行远程访问 可以使用*号
# port 6379 redis的端口号
# protected-mode yes 保护模式
# 下面的一些配置都是关于tcp的，反正也不懂略过
################################# GENERAL #####################################
# daemonize yes 以守护进程的方式运行(后台运行)，默认是no，需要改为yes
# supervised no 管理守护进程用的，一般不用动
# pidfile /var/run/redis_6379.pid 如果以后台方式运行，我们需要指定一个pid文件

# Specify the server verbosity level.
# This can be one of:
# debug (a lot of information, useful for development/testing)
# verbose (many rarely useful info, but not a mess like the debug level)
# notice (moderately verbose, what you want in production probably)
# warning (only very important / critical messages are logged)
# loglevel notice 日志级别，基本不用改

# logfile "" 生成的日志文件的名字
# databases 16 数据库的个数
# always-show-logo yes 是否显示redis的logo
################################ SNAPSHOTTING  ################################
# 这里的配置都是关于持久化的
# 含义解释：在规定的时间内，进行了多少次操作，则会持久化到文件，rdb、aof后面说
# save 900 1 900秒内至少1个key进行改变，则进行持久化，下面两个类似
# save 300 10
# save 60 10000
# stop-writes-on-bgsave-error yes 持久化出错之后是否继续写入工作
# rdbcompression yes 是否压缩rdb文件，需要消耗一些cpu资源
# rdbchecksum yes 是否校验rdb文件，同理也可能会消耗一些cpu资源
# dir ./ 持久化文件的保存目录
################################# REPLICATION #################################
# 主从复制相关配置，这里先不说，后面进行主从复制再来看
################################## SECURITY ###################################
# requirepass 123456 设置redis密码，一般不在配置文件中做，而是通过命令行，config set requirepass 密码的方式
################################### CLIENTS ####################################
# maxclients 10000 限制能连接上redis的最大客户端数量，
############################## MEMORY MANAGEMENT ################################
# 内存管理
# maxmemory <bytes> redis设置的最大内存容量

# MAXMEMORY POLICY: how Redis will select what to remove when maxmemory
# is reached. You can select one from the following behaviors:
#
# volatile-lru -> Evict using approximated LRU, only keys with an expire set.
# allkeys-lru -> Evict any key using approximated LRU.
# volatile-lfu -> Evict using approximated LFU, only keys with an expire set.
# allkeys-lfu -> Evict any key using approximated LFU.
# volatile-random -> Remove a random key having an expire set.
# allkeys-random -> Remove a random key, any key.
# volatile-ttl -> Remove the key with the nearest expire time (minor TTL)
# noeviction -> Don't evict anything, just return an error on write operations.
#
# LRU means Least Recently Used
# LFU means Least Frequently Used
#
# Both LRU, LFU and volatile-ttl are implemented using approximated
# randomized algorithms.
# maxmemory-policy noeviction 内存到达上限的处理策略
############################## APPEND ONLY MODE ###############################
# aof的配置
# appendonly no 是否开启aof模式 默认是不开启的 默认是rdb的方式进行持久化的，在大部分情况下完全够用
# appendfilename "appendonly.aof" aof持久化的文件的名字

# appendfsync always 每次修改都会同步，消耗性能
# appendfsync everysec 每秒执行一次，可能会丢失1秒的数据
# appendfsync no 不执行，相当于关闭
# 具体配置在redis持久化中详细说明

```

# redis的持久化

### RDB(redis database)

> redis会单独创建一个子线程来进行持久化，会先将数据写入到一个临时文件，待持久化过程都结束了，再用这个临时文件替换上一次持久化好的文件。整个过程中，redis主进程是不进行任何IO操作的，这就确保了极高的性能。如果需要进行大量数据的恢复，但对于数据恢复的敏感性不是非常高，那RDB方式要比AOF方式效率高。RDB的缺点是最后一次持久化后的数据有可能丢失。Redis默认就是使用RDB进行持久化，一般情况下不需要修改这个配置。

RDB的流程示意图

![](./images/4.png)

> RDB的触发机制

- 在主从复制场景下，如果从节点执行全量复制操作，则主节点会执行bgsave命令，并将rdb文件发送给从节点；

- 执行shutdown命令时，自动执行rdb持久化，如下图所示：

- 自动触发，最常见的情况是在配置文件中通过save m n，指定当m秒内发生n次变化时，会触发bgsave。

> 如何自动回复RDB文件

- 将.rdb文件放在配置文件指定的dir目录位置，在redis启动时就会自动加载.rdb中的数据

> RDB的优点

- RDB是一个紧凑压缩的二进制文件，代表Redis在某个时间点上的数据快照。非常适用于备份，全量复制等场景。比如每6个小时执行bgsave备份，并把RDB文件拷贝到远程机器或者文件系统中（如hdfs），用于灾难恢复。
- Redis加载RDB恢复数据远远快于AOF的方式。

> RDB的缺点

- RDB方式数据没办法做到实时持久化/秒级持久化。因为bgsave每次运行都要执行fork操作创建子进程，属于重量级操作，频繁执行成本过高。
- RDB文件使用特定二进制格式保存，Redis版本演进过程中有多个格式的RDB版本，存在老版本Redis服务无法兼容新版RDB格式的问题。
- 如果想要在Redis故障时，尽可能少的丢失数据，那么RDB没有AOF好。

### AOF(append only file)

> 以日志的形式，来记录每个写操作。将redis执行过的所有指令记录下来，只许追加文件但不许改写文件，redis启动之初会重新读取该文件重新构建数据。

AOF流程示意图

![](./images/5.png)

> AOF的其他配置

```properties
# auto-aof-rewrite-percentage 100 自动重写的配置
# auto-aof-rewrite-min-size 64mb 
```

> redis-check-aof修复aof文件

如果aof文件有错误，那么redis是启动不起来的，我们需要修复aof文件

> AOF的优点

- 该机制可以带来更高的数据安全性，即数据持久性。Redis中提供了3中同步策略，即每秒同步、每修改同步和不同步。事实上，每秒同步也是异步完成的，其效率也是非常高的，所差的是一旦系统出现宕机现象，那么这一秒钟之内修改的数据将会丢失。而每修改同步，我们可以将其视为同步持久化，即每次发生的数据变化都会被立即记录到磁盘中。可以预见，这种方式在效率上是最低的。至于无同步，无需多言，我想大家都能正确的理解它
- 由于该机制对日志文件的写入操作采用的是append模式，因此在写入过程中即使出现宕机现象，也不会破坏日志文件中已经存在的内容。然而如果我们本次操作只是写入了一半数据就出现了系统崩溃问题，不用担心，在Redis下一次启动之前，我们可以通过redis-check-aof工具来帮助我们解决数据一致性的问题。
- 如果日志过大，Redis可以自动启用rewrite机制。即Redis以append模式不断的将修改数据写入到老的磁盘文件中，同时Redis还会创建一个新的文件用于记录此期间有哪些修改命令被执行。因此在进行rewrite切换时可以更好的保证数据安全性。
-  AOF包含一个格式清晰、易于理解的日志文件用于记录所有的修改操作。事实上，我们也可以通过该文件完成数据的重建。

> AOF的缺点

- 对于相同数量的数据集而言，AOF文件通常要大于RDB文件。RDB 在恢复大数据集时的速度比 AOF 的恢复速度要快。
- 根据同步策略的不同，AOF在运行效率上往往会慢于RDB。总之，每秒同步策略的效率是比较高的，同步禁用策略的效率和RDB一样高效。













