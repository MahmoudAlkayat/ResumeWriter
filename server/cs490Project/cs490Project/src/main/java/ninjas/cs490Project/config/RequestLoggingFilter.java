package ninjas.cs490Project.config;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;


@Component
public class RequestLoggingFilter extends OncePerRequestFilter {


    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        logger.debug("Incoming request: {} {} | Headers: {}", request.getMethod(), request.getRequestURI(), request.getHeaderNames());
        filterChain.doFilter(request, response);
        logger.debug("Outgoing response: {} for {}", response.getStatus(), request.getRequestURI());
    }
}

