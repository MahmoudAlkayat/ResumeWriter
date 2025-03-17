package ninjas.cs490Project.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            private final Logger logger = LoggerFactory.getLogger(WebConfig.class);

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String allowedOrigins = "http://localhost:3000";
                String allowedMethods = "GET, POST, PUT, DELETE, OPTIONS";
                String allowedHeaders = "*";
                boolean allowCredentials = true;

                logger.info("Adding CORS mapping for /** with allowed origins: {}, allowed methods: {}, allowed headers: {}, allowCredentials: {}",
                        allowedOrigins, allowedMethods, allowedHeaders, allowCredentials);

                registry.addMapping("/**")
                        .allowedOrigins(allowedOrigins) // your front-end URL
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders(allowedHeaders)
                        .allowCredentials(allowCredentials);
            }
        };
    }
}
