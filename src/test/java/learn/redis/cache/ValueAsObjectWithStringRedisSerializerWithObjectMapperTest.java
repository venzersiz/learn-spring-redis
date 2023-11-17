package learn.redis.cache;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import learn.redis.annotation.domain.model.Address;
import learn.redis.annotation.domain.model.User;
import learn.redis.config.StringRedisSerializerRedisConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(StringRedisSerializerRedisConfig.class)
class ValueAsObjectWithStringRedisSerializerWithObjectMapperTest {

    @Autowired
    CacheManager cacheManager;

    @Test
    void valueAsObject() throws JsonProcessingException {
        Cache cache = cacheManager.getCache("e");

        List<Address> addresses = List.of(new Address("주소1"), new Address("주소2"));
        User user = new User(1L, "김백세", addresses);

        ObjectMapper mapper = new ObjectMapper();
        cache.put("user1", mapper.writeValueAsString(user));
        // 값은 아래처럼 직렬화되어 저장된다
        // {"seq":1,"name":"김백세","addresses":[{"name":"주소1"},{"name":"주소2"}]}

        String json = (String) cache.get("user1").get();
        User cachedUser = mapper.readValue(json, User.class);

        assertThat(cachedUser.getSeq()).isEqualTo(1L);
        assertThat(cachedUser.getName()).isEqualTo("김백세");
        assertThat(cachedUser.getAddresses().get(0).getName()).isEqualTo("주소1");
        assertThat(cachedUser.getAddresses().get(1).getName()).isEqualTo("주소2");
    }
}
