package learn.redis.annotation.service;

import learn.redis.annotation.domain.model.User;
import learn.redis.annotation.domain.model.User2;
import learn.redis.annotation.domain.repository.DummyUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final DummyUserRepository userRepository;

    public User findOne(long id) {
        System.out.println("비즈니스 로직");

        return userRepository.findOne(id);
    }

    public User2 findUser2(long id) {
        System.out.println("비즈니스 로직");

        return userRepository.findUser2(id);
    }
}
