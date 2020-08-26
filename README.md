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

