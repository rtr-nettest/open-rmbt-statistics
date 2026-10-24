package at.rtr.rmbt;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;

/**
 * Supplies a simple in-memory {@link CacheManager} for {@code @WebMvcTest} slices. The Spring Boot 4
 * web slice no longer registers a CacheManager, but {@code StatisticServerApplication} is annotated
 * with {@code @EnableCaching}, which requires one in the context. The controllers under test mock
 * their services, so this cache is never actually exercised - it only satisfies the context.
 */
@TestConfiguration
public class TestCacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }
}
