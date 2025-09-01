package com.vladte.devhack.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Configuration class for Redis caching with multiple cache managers.
 * Provides three cache managers with different TTL periods:
 * - Short-term cache: 30 minutes
 * - Medium-term cache: 1 hour
 * - Long-term cache: 24 hours
 */
@Configuration
@EnableCaching
public class CommonCacheConfig {

    /**
     * Primary cache manager for short-term caching (30 minutes).
     * Used for frequently changing data that needs quick access.
     */
    @Bean
    @Primary
    public CacheManager shortTermCacheManager(RedisConnectionFactory connectionFactory) {
        return createCacheManager(connectionFactory, Duration.ofMinutes(30));
    }

    /**
     * Creates a Redis cache manager with the specified TTL.
     *
     * @param connectionFactory Redis connection factory
     * @param ttl               Time-to-live duration for cached entries
     * @return Configured RedisCacheManager
     */
    private RedisCacheManager createCacheManager(RedisConnectionFactory connectionFactory, Duration ttl) {
        // Create ObjectMapper with JSR310 module for LocalDateTime support and type information
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);

        // Create GenericJackson2JsonRedisSerializer with custom ObjectMapper
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}