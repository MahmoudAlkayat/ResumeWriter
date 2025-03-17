package ninjas.cs490Project.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SecurityBeansConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityBeansConfig.class);

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        logger.info("Creating BCryptPasswordEncoder bean.");
        return new BCryptPasswordEncoder();
    }
}
