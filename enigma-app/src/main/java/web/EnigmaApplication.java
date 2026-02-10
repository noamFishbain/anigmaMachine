package web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"web", "controllers", "service"})
public class EnigmaApplication {
    public static void main(String[] args) {
        SpringApplication.run(EnigmaApplication.class, args);
    }
}