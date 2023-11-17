package learn.redis.annotation.domain.model;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class SerializableAddress implements Serializable {

    private String name;

    public SerializableAddress(String name) {
        this.name = name;
    }
}
