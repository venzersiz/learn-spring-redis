package learn.redis.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import learn.redis.annotation.domain.model.Address;
import learn.redis.annotation.domain.model.User;
import learn.redis.annotation.domain.model.User2;
import learn.redis.config.CustomGenericJackson2JsonRedisSerializerRedisConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(CustomGenericJackson2JsonRedisSerializerRedisConfig.class)
class CustomGenericJackson2JsonRedisSerializerTest {

    @Autowired
    CacheManager cacheManager;

    @Test
    void serialize() {
        Cache cache = cacheManager.getCache("h");

        List<Address> addresses = List.of(new Address("주소1"), new Address("주소2"));
        User user = new User(1L, "김백세", addresses);

        cache.put("user1", user);
        // 값은 아래처럼 직렬화되어 저장된다
        // {
        //   "@type" : "user",
        //   "seq" : 1,
        //   "name" : "김백세",
        //   "addresses" : [ "java.util.ImmutableCollections$List12", [ {
        //     "@type" : "address",
        //     "name" : "주소1"
        //   }, {
        //     "@type" : "address",
        //     "name" : "주소2"
        //   } ] ]
        // }
    }

    // 이 테스트를 실행하려면 User 클래스의 @JsonTypeName("user")를 제거하자
    // 다른 애플리케이션이라고 가정을 해야하기 때문이고 한 애플리케이션에서 같은 ID를 가졌기에 예외가 나는 것
    @Test
    void deserialize() {
        // 다른 애플리케이션에서 캐쉬 조회를 한다고 가정
        Cache cache = cacheManager.getCache("h");

        User2 cachedUser = (User2) cache.get("user1").get();
        assertThat(cachedUser.getSeq()).isEqualTo(1L);
        assertThat(cachedUser.getName()).isEqualTo("김백세");
        assertThat(cachedUser.getAddresses().get(0).getName()).isEqualTo("주소1");
        assertThat(cachedUser.getAddresses().get(1).getName()).isEqualTo("주소2");
    }
}
