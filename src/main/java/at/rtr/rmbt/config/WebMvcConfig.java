package at.rtr.rmbt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Boolean ALLOW_CREDENTIALS = false;
    private static final Long MAX_AGENT = 60L;
    private static final List<String> ALLOW_METHODS = List.of("GET", "POST", "OPTIONS");
    private static final List<String> ALLOW_HEADERS = List.of("Content-Type");
    private static final List<String> ALLOW_ORIGIN = List.of("*");


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
                .addMapping("/**")
                .allowCredentials(ALLOW_CREDENTIALS)
                .allowedMethods(ALLOW_METHODS.toArray(String[]::new))
                .maxAge(MAX_AGENT)
                .allowedOrigins(ALLOW_ORIGIN.toArray(String[]::new))
                .allowedHeaders(ALLOW_HEADERS.toArray(String[]::new));
    }

    @Bean
    public Filter corsFilter() {
        return (request, response, chain) -> {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse res = (HttpServletResponse) response;

            res.setHeader("Access-Control-Allow-Origin", String.join(", ", ALLOW_ORIGIN));
            res.setHeader("Access-Control-Allow-Credentials", ALLOW_CREDENTIALS.toString());
            res.setHeader("Access-Control-Allow-Methods", String.join(", ", ALLOW_METHODS));
            res.setHeader("Access-Control-Max-Age", MAX_AGENT.toString());
            res.setHeader("Access-Control-Allow-Headers", String.join(", ", ALLOW_HEADERS));

            chain.doFilter(req, res);
        };
    }
}
