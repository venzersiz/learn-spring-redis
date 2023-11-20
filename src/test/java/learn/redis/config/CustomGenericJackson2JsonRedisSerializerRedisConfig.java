package learn.redis.config;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTypeResolverBuilder;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.cache.CacheProperties.Redis;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager.RedisCacheManagerBuilder;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.RedisSerializer;

@EnableCaching
public class CustomGenericJackson2JsonRedisSerializerRedisConfig {

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory,
                                               CacheProperties cacheProperties,
                                               CustomRedisProperties customRedisProperties) {

        String typeNameSearchPackage = "learn.redis";
        RedisSerializer valueSerializer = genericJackson2JsonRedisSerializer(typeNameSearchPackage);

        RedisCacheConfiguration defaultCacheConfig = redisCacheConfiguration(valueSerializer, cacheProperties.getRedis());

        return RedisCacheManagerBuilder.fromConnectionFactory(connectionFactory)
                                       .cacheDefaults(defaultCacheConfig)
                                       .withInitialCacheConfigurations(initialCacheConfig(customRedisProperties.createPropsByCacheKey(), valueSerializer))
                                       .build();
    }

    private RedisCacheConfiguration redisCacheConfiguration(RedisSerializer<?> valueSerializer, Redis redisProps) {

        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                                                                     .serializeValuesWith(SerializationPair.fromSerializer(valueSerializer));

        if (redisProps.getTimeToLive() != null) {
            cacheConfig = cacheConfig.entryTtl(redisProps.getTimeToLive());
        }

        return cacheConfig;
    }

    private Map<String, RedisCacheConfiguration> initialCacheConfig(Map<String, Redis> propsByCacheKey, RedisSerializer valueSerializer) {

        return propsByCacheKey.entrySet()
                              .stream()
                              .collect(toMap(Entry::getKey, entry -> redisCacheConfiguration(valueSerializer, entry.getValue())));
    }

    @Bean
    @ConfigurationProperties("spring.cache")
    @ConditionalOnMissingBean(CacheProperties.class)
    public CacheProperties cacheProperties() {
        return new CacheProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "krevis.cache.redis")
    public CustomRedisProperties customRedisProperties() {
        return new CustomRedisProperties();
    }

    private GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer(String typeNameSearchPackage) {

        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

        TypeResolverBuilder<StdTypeResolverBuilder> typer =
            new DefaultTypeResolverBuilder(DefaultTyping.NON_FINAL, objectMapper.getPolymorphicTypeValidator());
        typer = typer.init(Id.NAME, new DynamicTypeIdResolver(typeNameSearchPackage));
        typer = typer.inclusion(JsonTypeInfo.As.PROPERTY);

        objectMapper.setDefaultTyping(typer);
        objectMapper.addMixIn(List.class, ListMixIn.class);

        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    static class DynamicTypeIdResolver extends TypeIdResolverBase {

        private final Map<String, Class<?>> classByIdMap;

        public DynamicTypeIdResolver(String typeNameSearchPackage) {

            // 기본 필터는 @Component 기반의 빈들을 등록하기 때문에 사용하지 않음
            ClassPathScanningCandidateComponentProvider componentProvider =
                new ClassPathScanningCandidateComponentProvider(false);
            // @JsonTypeName이 달린 컴포넌트만 사용하도록 필터 추가
            componentProvider.addIncludeFilter(new AnnotationTypeFilter(JsonTypeName.class));

            this.classByIdMap = componentProvider.findCandidateComponents(typeNameSearchPackage)
                                                 .stream()
                                                 .map(BeanDefinition::getBeanClassName)
                                                 .map(className -> {
                                                     try {
                                                         return Class.forName(className);
                                                     } catch (ClassNotFoundException e) {
                                                         throw new RuntimeException(e);
                                                     }
                                                 })
                                                 .collect(toMap(aClass -> aClass.getDeclaredAnnotation(JsonTypeName.class).value(), identity()));
        }

        @Override
        public String idFromValue(Object value) {
            return idFromValueAndType(value, value.getClass());
        }

        @Override
        public String idFromValueAndType(Object value, Class<?> suggestedType) {
            return suggestedType.getDeclaredAnnotation(JsonTypeName.class).value();
        }

        @Override
        public Id getMechanism() {
            return null;
        }

        @Override
        public JavaType typeFromId(DatabindContext context, String id) {

            return context.constructType(classByIdMap.get(id));
        }
    }

    @JsonTypeInfo(use = Id.CLASS)
    interface ListMixIn {

    }

    @Getter
    @Setter
    static class CustomRedisProperties extends CacheProperties.Redis {

        private List<Item> caches = new ArrayList<>();

        public Map<String, CacheProperties.Redis> createPropsByCacheKey() {
            return caches.stream().collect(toMap(Item::getName, identity()));
        }

        @Getter
        @Setter
        static class Item extends CacheProperties.Redis {

            private String name;
        }
    }
}
