package at.rtr.rmbt.config;

import at.rtr.rmbt.constant.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Value("${redis.port}")
    private Integer redisPort;

    @Value("${redis.host}")
    private String redisHost;

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(5))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(JedisConnectionFactory jedisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory);
        template.setStringSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        return new JedisConnectionFactory(new RedisStandaloneConfiguration(redisHost, redisPort));
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return (builder) -> builder
                .withCacheConfiguration(Constants.OPENTESTS_HISTOGRAM_CACHE_NAME,
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(Constants.OPENTESTS_HISTOGRAM_CACHE_EXPIRE_SECONDS)))
                .withCacheConfiguration(Constants.OPENTESTS_INTRADAY_CACHE_NAME,
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(Constants.OPENTESTS_INTRADAY_CACHE_EXPIRE_SECONDS)))
                .withCacheConfiguration(Constants.OPENTESTS_STATISTIC_CACHE_NAME,
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(Constants.OPENTESTS_STATISTICS_CACHE_EXPIRE_SECONDS)))
                .withCacheConfiguration(Constants.STATISTIC_CACHE_NAME,
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(Constants.CACHE_EXPIRE_HOURS)))
                .withCacheConfiguration(Constants.STATISTICS_STALE_CACHE_NAME,
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(Constants.CACHE_EXPIRE_HOURS)));
    }
}
