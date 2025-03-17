package ninjas.cs490Project.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(CorsConfig.class);

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String allowedOrigins = "http://localhost:3000";
        String allowedMethods = "GET, POST, PUT, DELETE, OPTIONS";
        long maxAge = 3600;
        boolean allowCredentials = true;
        logger.info("Adding CORS mapping for /** with allowed origins: {}, allowed methods: {}, allowCredentials: {}, maxAge: {} seconds",
                allowedOrigins, allowedMethods, allowCredentials, maxAge);

        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(maxAge);
    }
}
