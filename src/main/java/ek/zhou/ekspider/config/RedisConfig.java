package ek.zhou.ekspider.config;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import ek.zhou.ekspider.entity.RedisEntity;
import ek.zhou.ekspider.entity.Spider;
import ek.zhou.ekspider.util.JsonUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.IOException;

@Configuration
public class RedisConfig {
    public static Long expireTime;
    @Bean
    public RedisConnectionFactory redisConnectionFactory(){
        RedisEntity redisEntity = null;
        try {
            redisEntity = JsonUtils.readJsonFromClassPath("redis.json", RedisEntity.class);
        } catch (IOException e) {
            redisEntity = new RedisEntity();
        }
        JedisConnectionFactory connectionFactory = new JedisConnectionFactory();
        connectionFactory.setPort(redisEntity.getPort());
        connectionFactory.setHostName(redisEntity.getHostname());
        connectionFactory.setDatabase(redisEntity.getDatabase());
        connectionFactory.setPassword(redisEntity.getPassword());
        //配置连接池属性
        connectionFactory.setTimeout(redisEntity.getTimeout());
        connectionFactory.getPoolConfig().setMaxIdle(redisEntity.getMaxIdle());
        connectionFactory.getPoolConfig().setMaxTotal(redisEntity.getMaxTotal());
        connectionFactory.getPoolConfig().setMaxWaitMillis(redisEntity.getMaxWaitMillis());
        connectionFactory.getPoolConfig().setMinEvictableIdleTimeMillis(redisEntity.getMinEvictableIdleTimeMillis());
        connectionFactory.getPoolConfig().setNumTestsPerEvictionRun(redisEntity.getNumTestsPerEvictionRun());
        connectionFactory.getPoolConfig().setTimeBetweenEvictionRunsMillis(redisEntity.getTimeBetweenEvictionRunsMillis());
        connectionFactory.getPoolConfig().setTestOnBorrow(redisEntity.getTestOnBorrow());
        connectionFactory.getPoolConfig().setTestWhileIdle(redisEntity.getTestWhileIdle());
        expireTime = redisEntity.getExpireTime();
        return connectionFactory;
    }
    @Bean
    public RedisTemplate redisTemplate(
            RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate template = new RedisTemplate<>();

        //使用fastjson序列化
        FastJsonRedisSerializer fastJsonRedisSerializer = new FastJsonRedisSerializer(Object.class);
        // value值的序列化采用fastJsonRedisSerializer
        template.setValueSerializer(fastJsonRedisSerializer);
        template.setHashValueSerializer(fastJsonRedisSerializer);
        // key的序列化采用StringRedisSerializer
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(
            RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

}