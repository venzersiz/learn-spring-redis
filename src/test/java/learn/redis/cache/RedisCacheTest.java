package learn.redis.cache;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheManager;

@SpringBootTest
class RedisCacheTest {

    @Autowired
    CacheManager cacheManager;

    @Test
    void getCache() {
        assertThat(cacheManager).isInstanceOf(RedisCacheManager.class);

        Cache cacheA = cacheManager.getCache("a"); // application.yml
        assertThat(cacheA).isInstanceOf(RedisCache.class);

        Cache cacheB = cacheManager.getCache("b"); // application.yml
        assertThat(cacheB).isInstanceOf(RedisCache.class);

        Cache cacheC = cacheManager.getCache("c");
        assertThat(cacheC).isInstanceOf(RedisCache.class);
    }

    @Test
    void putAndGet() {
        Cache cacheA = cacheManager.getCache("a"); // application.yml

        cacheA.put("key키1!", "value값1!"); // a::key키1!라는 키로 저장됨
        assertThat(cacheA.get("key키1!").get()).isEqualTo("value값1!");
        assertThat(cacheA.get("key키2@")).isNull();

        Cache cacheC = cacheManager.getCache("c");

        cacheC.put("key키1!", "value값1!"); // c::key키1!라는 키로 저장됨
        assertThat(cacheC.get("key키1!").get()).isEqualTo("value값1!");
        assertThat(cacheC.get("key키2@")).isNull();
    }

    @Test
    void expiry() throws InterruptedException {
        Cache cache = cacheManager.getCache("anything"); // TTL: 5초

        cache.put("key", "value");
        assertThat(cache.get("key").get()).isEqualTo("value");

        Thread.sleep(5_000);

        assertThat(cache.get("key")).isNull(); // 캐쉬 만료됨
        // spring.cache.redis.time-to-live 프라퍼티로 설정한 값은 spring.cache.cache-names 프라퍼티로 설정한 캐쉬에 국한되지 않고,
        // 전역적으로 동작한다는 것을 확인할 수 있다
    }
}