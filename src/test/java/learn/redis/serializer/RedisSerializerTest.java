package learn.redis.serializer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.RedisSerializer;

class RedisSerializerTest {

    @Test
    void stringSerializer() {
        RedisSerializer<String> serializer = RedisSerializer.string();

        byte[] bytes = serializer.serialize("key키1!");

        assertThat(new String(bytes)).isEqualTo("key키1!");
    }
}
