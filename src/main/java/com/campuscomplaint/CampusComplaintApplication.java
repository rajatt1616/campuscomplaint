package com.campuscomplaint;

import com.campuscomplaint.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CampusComplaintApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusComplaintApplication.class, args);
    }

    @Bean
    CommandLineRunner seed(AuthService auth) {
        return args -> auth.seedIfEmpty();
    }
}
