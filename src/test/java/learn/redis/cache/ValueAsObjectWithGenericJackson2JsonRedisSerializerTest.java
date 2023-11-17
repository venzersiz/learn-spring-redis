package learn.redis.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import learn.redis.annotation.domain.model.Address;
import learn.redis.annotation.domain.model.User;
import learn.redis.annotation.domain.model.User2;
import learn.redis.config.GenericJackson2JsonRedisSerializerRedisConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(GenericJackson2JsonRedisSerializerRedisConfig.class)
class ValueAsObjectWithGenericJackson2JsonRedisSerializerTest {

    @Autowired
    CacheManager cacheManager;

    @Test
    void valueAsObject() {
        Cache cache = cacheManager.getCache("f");

        List<Address> addresses = List.of(new Address("주소1"), new Address("주소2"));
        User user = new User(1L, "김백세", addresses);

        cache.put("user1", user);
        // 값은 아래처럼 직렬화되어 저장된다
        // {
        //   "@class": "learn.redis.annotation.domain.model.User",
        //   "seq": 1,
        //   "name": "김백세",
        //   "addresses": [
        //     "java.util.ImmutableCollections$List12",
        //     [
        //       {
        //         "@class": "learn.redis.annotation.domain.model.Address",
        //         "name": "주소1"
        //       },
        //       {
        //         "@class": "learn.redis.annotation.domain.model.Address",
        //         "name": "주소2"
        //       }
        //     ]
        //   ]
        // }

        User cachedUser = (User) cache.get("user1").get();
        assertThat(cachedUser.getSeq()).isEqualTo(1L);
        assertThat(cachedUser.getName()).isEqualTo("김백세");
        assertThat(cachedUser.getAddresses().get(0).getName()).isEqualTo("주소1");
        assertThat(cachedUser.getAddresses().get(1).getName()).isEqualTo("주소2");
    }

    @Test
    void genericJackson2JsonRedisSerializerWithDifferentType() {
        Cache cache = cacheManager.getCache("e");

        List<Address> addresses = List.of(new Address("주소1"), new Address("주소2"));
        User user = new User(1L, "김백세", addresses);

        cache.put("user1", user);

        // 다른 애플리케이션에서 캐쉬 조회를 한다고 가정
        assertThatThrownBy(() -> {
            User2 cachedUser = (User2) cache.get("user1").get();
        }).isInstanceOf(ClassCastException.class);
        // 동일 구조의 모델임에도 불구하고 클래스명이 달라서 예외가 발생한다
        // 클래스명이 동일하고 패키지가 달라도 예외 발생
    }
}
