package com.example.sclms_apigateway.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisConfig {

        /* @Value("${spring.data.redis.host:localhost}")
        private String redisHost;
        @Value("${spring.data.redis.port:6379}")
        private int redisPort;
        @Value("${spring.data.redis.timeout:2000}")
        private int timeout;
*/

    /*@Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory(){
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisHost, redisPort);
        LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration.defaultConfiguration();

        factory.setTimeout(timeout);
        factory.afterPropertiesSet();
        return factory;
}*/

    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        StringRedisSerializer serializer = new StringRedisSerializer();
        RedisSerializationContext<String, String> serializationContext =
                RedisSerializationContext.<String, String>newSerializationContext(serializer)
                        .key(serializer)
                        .value(serializer)
                        .hashKey(serializer)
                        .hashValue(serializer)
                        .build();

        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }


}
