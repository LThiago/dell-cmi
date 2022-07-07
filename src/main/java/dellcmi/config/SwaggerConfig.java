package dellcmi.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class SwaggerConfig {

    @Bean
    public OpenAPI springDocOpenApi() {
        return new OpenAPI();
    }
}