package learn.redis.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import learn.redis.annotation.domain.model.SerializableAddress;
import learn.redis.annotation.domain.model.SerializableUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootTest
@EnableCaching
//@Import(JdkSerializationRedisSerializerRedisConfig.class) // 기본 설정이라 이 부분은 없어도 무관
class JdkSerializationRedisSerializerTest {

    @Autowired
    CacheManager cacheManager;

    @Test
    void valueAsObject() {
        Cache cache = cacheManager.getCache("d");

        List<SerializableAddress> addresses = List.of(new SerializableAddress("주소1"), new SerializableAddress("주소2"));
        SerializableUser user = new SerializableUser(1L, "김백세", addresses);

        cache.put("user1", user); // d::user1이라는 키로 저장됨
        // 값은 아래처럼 직렬화되어 저장된다
        // ��sr(learn.redis.annotation.domain.model.User:V����vL	addressestLjava/util/List;LnametLjava/lang/String;LseqtLjava/lang/Long;xpsrjava.util.CollSerW���:Itagxpwsr+learn.redis.annotation.domain.model.Address���M�9HLnameq~xpt주소1sq~t주소2xt	김백세srjava.lang.Long;��̏#�Jvaluexrjava.lang.Number���
        //                                                                                                                                      ��xp
        SerializableUser cachedUser = (SerializableUser) cache.get("user1").get();

        assertThat(cachedUser.getSeq()).isEqualTo(1L);
        assertThat(cachedUser.getName()).isEqualTo("김백세");
        assertThat(cachedUser.getAddresses().get(0).getName()).isEqualTo("주소1");
        assertThat(cachedUser.getAddresses().get(1).getName()).isEqualTo("주소2");
    }
}
