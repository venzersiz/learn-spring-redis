package learn.redis.config;

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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.RedisSerializer;

@EnableCaching
public class CustomGenericJackson2JsonRedisSerializerRedisConfig {

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        String typeNameSearchPackage = "learn.redis";

        RedisSerializer valueSerializer = genericJackson2JsonRedisSerializer(typeNameSearchPackage);

        return RedisCacheConfiguration.defaultCacheConfig()
                                      .serializeValuesWith(SerializationPair.fromSerializer(valueSerializer));
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
                                                 .collect(toMap(aClass -> aClass.getDeclaredAnnotation(JsonTypeName.class).value(), Function.identity()));
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
}
