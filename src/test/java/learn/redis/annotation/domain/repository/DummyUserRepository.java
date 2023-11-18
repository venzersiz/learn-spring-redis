package learn.redis.annotation.domain.repository;

import java.util.List;
import learn.redis.annotation.domain.model.Address;
import learn.redis.annotation.domain.model.User;
import learn.redis.annotation.domain.model.User2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

/**
 * DB 조회를 가장
 */
@Repository
public class DummyUserRepository {

    // 애너테이션을 사용해 선언형으로 특정 캐쉬와 연결
    @Cacheable(cacheNames = "user", key = "#id") // user라는 이름의 캐쉬에서 1이라는 키로 저장
    public User findOne(long id) {
        System.out.println("DB 조회");

        if (id == 1L) {
            List<Address> addresses = List.of(new Address("주소1"), new Address("주소2"));
            return new User(1L, "김백세", addresses);
        } else {
            return null;
        }
    }

    @Cacheable(cacheNames = "user", key = "#id") // user라는 이름의 캐쉬에서 1이라는 키로 저장
    public User2 findUser2(long id) {
        System.out.println("DB 조회");

        if (id == 1L) {
            List<Address> addresses = List.of(new Address("주소1"), new Address("주소2"));
            return new User2(1L, "김백세", addresses);
        } else {
            return null;
        }
    }
}
