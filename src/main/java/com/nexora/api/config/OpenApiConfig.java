
package com.nexora.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI nexoraOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Nexora API")
                        .version("1.0.0")
                        .description("""
                                REST API for independent professionals and small businesses.

                                Nexora allows users to manage clients, services, appointments,
                                appointment rescheduling and cancellation, and payment tracking.

                                Business rules include:
                                - Prevention of overlapping appointments.
                                - Appointment rescheduling with traceability.
                                - Payment deadlines of 48 hours.
                                - Payment receipt submission and review.
                                """));
    }
}
