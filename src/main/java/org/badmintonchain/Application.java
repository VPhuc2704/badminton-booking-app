package org.badmintonchain;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRabbit
public class Application {
    public static void main(String[] args) {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .systemProperties() // put into System properties
                    .load();
            
        } catch (Exception e) {
            System.err.println("Warning: failed to load .env: " + e.getMessage());
        }

        SpringApplication.run(Application.class, args);
    }
}