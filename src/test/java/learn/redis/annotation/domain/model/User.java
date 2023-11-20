package learn.redis.annotation.domain.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@JsonTypeName("user")
public class User {

    private Long seq;

    private String name;

    private List<Address> addresses;

    public User(Long seq, String name, List<Address> addresses) {
        this.seq = seq;
        this.name = name;
        this.addresses = addresses;
    }
}
