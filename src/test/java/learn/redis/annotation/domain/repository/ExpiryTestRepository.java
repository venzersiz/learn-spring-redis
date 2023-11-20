package learn.redis.annotation.domain.repository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

@Repository
public class ExpiryTestRepository {

    @Cacheable(cacheNames = "someapi:a")
    public String someApiA(String p) {
        return "someapi:a";
    }

    @Cacheable(cacheNames = "someapi:b")
    public String someApiB(String p) {
        return "someapi:b";
    }
}
