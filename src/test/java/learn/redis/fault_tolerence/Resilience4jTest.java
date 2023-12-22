package learn.redis.fault_tolerence;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@SpringBootTest
@Slf4j
class Resilience4jTest {

    @Autowired
    TestService service;

    @Test
    void t1() {
        log.info("시도 1");
        assertThat(service.logic()).isEqualTo("Recovery");

        log.info("시도 2");
        assertThat(service.logic()).isEqualTo("Recovery");
    }

    @TestConfiguration
    @ComponentScan(basePackages = "learn.redis.fault_tolerence")
    @EnableCaching
    static class TestConfig {

    }

    @Service
    @RequiredArgsConstructor
    static class TestService {

        private final TestRepository repo;

        String logic() {
            log.info("Service 호출");
            return repo.findData();
        }
    }

    @Repository
    static class TestRepository {

        @Cacheable(cacheNames = "findData")
        @CircuitBreaker(name = "findData", fallbackMethod = "findDataFallback")
        public String findData() {
            log.info("DB 호출");
            return "Success";
        }

        @SuppressWarnings("unused")
        public String findDataFallback(Exception e) {
            //log.info("Fallback 실행", e);
            log.info("Fallback 실행");
            return "Recovery";
        }
    }
}
