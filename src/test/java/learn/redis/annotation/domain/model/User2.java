package learn.redis.annotation.domain.model;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
//@JsonTypeName("user")
public class User2 {

    private Long seq;

    private String name;

    private List<Address> addresses;

    public User2(Long seq, String name, List<Address> addresses) {
        this.seq = seq;
        this.name = name;
        this.addresses = addresses;
    }
}
