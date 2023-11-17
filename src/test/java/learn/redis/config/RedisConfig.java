package learn.redis.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTypeResolverBuilder;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

//@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(createObjectMapper());

        return RedisCacheConfiguration.defaultCacheConfig()
                                      //.serializeValuesWith(SerializationPair.fromSerializer(RedisSerializer.string()));
                                      //.serializeValuesWith(SerializationPair.fromSerializer(RedisSerializer.json()));
                                      .serializeValuesWith(SerializationPair.fromSerializer(jsonSerializer));
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        TypeResolverBuilder<StdTypeResolverBuilder> typer =
            new DefaultTypeResolverBuilder(DefaultTyping.NON_FINAL, mapper.getPolymorphicTypeValidator());
        // Long 타입 필드가 있을 때 Default Typing: EVERYTHING과 NON_FINAL의 차이
        // EVERYTHING: "seq":["Long",1]
        // NON_FINAL: "seq": 1

        typer = typer.init(Id.NAME, null);
        typer = typer.inclusion(As.PROPERTY);

        mapper.setDefaultTyping(typer);
        return mapper;
    }
}
