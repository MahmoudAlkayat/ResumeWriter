package ninjas.cs490Project.config;// Example of a simple CORS config
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000") // or "*"
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
