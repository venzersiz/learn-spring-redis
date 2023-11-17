package learn.redis.annotation.domain.model;

import java.io.Serializable;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public class SerializableUser implements Serializable {

    private Long seq;

    private String name;

    private List<SerializableAddress> addresses;

    public SerializableUser(Long seq, String name, List<SerializableAddress> addresses) {
        this.seq = seq;
        this.name = name;
        this.addresses = addresses;
    }
}
