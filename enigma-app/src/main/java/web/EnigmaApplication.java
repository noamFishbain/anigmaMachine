package web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"web", "controllers", "service", "dal"})
@EnableJpaRepositories(basePackages = "dal.repositories")
@EntityScan(basePackages = "dal.models")
public class EnigmaApplication {
    public static void main(String[] args) {
        SpringApplication.run(EnigmaApplication.class, args);
    }
}