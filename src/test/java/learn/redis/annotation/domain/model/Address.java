package learn.redis.annotation.domain.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@JsonTypeName("address")
public class Address {

    private String name;

    public Address(String name) {
        this.name = name;
    }
}
