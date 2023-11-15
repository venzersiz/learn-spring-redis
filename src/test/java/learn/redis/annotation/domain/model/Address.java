package learn.redis.annotation.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Address {

    private String name;

    public Address(String name) {
        this.name = name;
    }
}
