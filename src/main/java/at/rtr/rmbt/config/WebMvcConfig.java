package at.rtr.rmbt.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableWebMvc
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Boolean ALLOW_CREDENTIALS = false;
    private static final Long MAX_AGENT = 60L;
    private static final List<String> ALLOW_METHODS = List.of("GET", "POST", "OPTIONS", "HEAD");
    private static final List<String> ALLOW_HEADERS = List.of("Content-Type");
    private static final List<String> ALLOW_ORIGIN = List.of("*");

    /**
     * The Spring Boot auto-configured Jackson 2 ObjectMapper (com.fasterxml.*), provided by the
     * {@code spring-boot-jackson2} module and configured via the {@code spring.jackson2.*} properties.
     * It is a distinct type from the Boot 4 default Jackson 3 mapper, so there is no bean ambiguity.
     */
    private final ObjectMapper jackson2ObjectMapper;

    public WebMvcConfig(ObjectMapper jackson2ObjectMapper) {
        this.jackson2ObjectMapper = jackson2ObjectMapper;
    }

    /**
     * Forces the REST API to use Jackson 2 for JSON. {@code @EnableWebMvc} disables Spring Boot's
     * message-converter auto-configuration, so {@code spring.http.converters.preferred-json-mapper}
     * has no effect and Spring MVC would otherwise install the Framework 7 default Jackson 3 converter.
     * Jackson 3 cannot construct this app's Lombok @Builder/final-field request DTOs (StatisticRequest,
     * CapabilitiesRequest, ...) and rejects every request body with 400. Prepending a Jackson 2
     * converter makes it win for JSON (converters are tried in order), restoring the pre-Boot-4 API.
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(0, new MappingJackson2HttpMessageConverter(jackson2ObjectMapper));
    }


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
