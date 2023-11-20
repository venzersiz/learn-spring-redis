package learn.redis.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import learn.redis.annotation.domain.model.User;
import learn.redis.annotation.domain.model.User2;
import learn.redis.annotation.domain.repository.DummyUserRepository;
import learn.redis.annotation.domain.repository.ExpiryTestRepository;
import learn.redis.annotation.service.UserService;
import learn.redis.config.CustomGenericJackson2JsonRedisSerializerRedisConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(CustomGenericJackson2JsonRedisSerializerRedisConfig.class)
class DeclarativeAnnotationBasedCachingTest {

    @Autowired
    UserService userService;

    @Autowired
    DummyUserRepository userRepository;

    @Autowired
    ExpiryTestRepository expiryTestRepository;

    // 이 테스트를 실행하려면 User 클래스에 @JsonTypeName("user")을 달고, User2 클래스의 @JsonTypeName("user")를 제거하자
    // 다른 애플리케이션이라고 가정을 해야하기 때문이고 한 애플리케이션에서 같은 ID를 가졌기에 예외가 나는 것
    @Test
    void declarativeAnnotationBasedCaching() {
        User user = userService.findOne(1L);
        // 1. 비즈니스 로직
        // 2. DB 조회
        // -> 캐쉬에 없어 DB 조회

        // user::1이 키인 캐쉬의 값은 아래처럼 직렬화되어 저장된다
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

        assertThat(user.getSeq()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("김백세");
        assertThat(user.getAddresses().get(0).getName()).isEqualTo("주소1");
        assertThat(user.getAddresses().get(1).getName()).isEqualTo("주소2");

        User cachedUser = userService.findOne(1L);
        // 1. 비즈니스 로직
        // -> 캐쉬에 있어 DB 조회 X
        assertThat(user).isNotEqualTo(cachedUser); // 캐쉬 값을 가져오긴 하는데 인스턴스 주소값이 다르다

        userService.findOne(2L);
        // 1. 비즈니스 로직
        // 2. DB 조회
        // -> 캐쉬에 없어 DB 조회

        // user::2가 키인 캐쉬의 값은 아래처럼 직렬화되어 저장된다
        // ��sr+org.springframework.cache.support.NullValuexp
    }

    // 이 테스트를 실행하려면 User2 클래스에 @JsonTypeName("user")을 달고, User 클래스의 @JsonTypeName("user")를 제거하자
    // 다른 애플리케이션이라고 가정을 해야하기 때문이고 한 애플리케이션에서 같은 ID를 가졌기에 예외가 나는 것
    @Test
    void findUserFromOtherApplications() {
        User2 user = userService.findUser2(1L);

        assertThat(user.getSeq()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("김백세");
        assertThat(user.getAddresses().get(0).getName()).isEqualTo("주소1");
        assertThat(user.getAddresses().get(1).getName()).isEqualTo("주소2");
    }

    @Test
    void cacheKey() {
        assertThat(userRepository.findUser3(3L)).isNotNull();
        // 생성된 캐쉬 키: someapi:user::3

        assertThat(userRepository.findUser4()).isNotNull();
        // 생성된 캐쉬 키: someapi:user4::SimpleKey []
    }

    @Test
    void expiryByCache() {
        expiryTestRepository.someApiA("에이");
        expiryTestRepository.someApiB("비");

        // Redis CLI로 확인해보면 각각 5초, 10초 후 캐쉬 제거됨
    }
}
