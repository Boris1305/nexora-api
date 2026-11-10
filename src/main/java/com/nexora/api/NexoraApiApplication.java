
package com.nexora.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NexoraApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexoraApiApplication.class, args);
    }
}
