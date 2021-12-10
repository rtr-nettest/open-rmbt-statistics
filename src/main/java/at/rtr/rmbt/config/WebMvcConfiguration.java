package at.rtr.rmbt.config;

import at.rtr.rmbt.constant.URIConstants;
import com.auth0.spring.security.api.JwtWebSecurityConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;


@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        final PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver();
        pageableResolver.setOneIndexedParameters(true);
        argumentResolvers.add(pageableResolver);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Configuration
    public static class WebSecurityConfig extends WebSecurityConfigurerAdapter {

        private static final String[] clients = new String[]{"client:specure", "client:rtr"};

        @Value("${auth0.apiAudience}")
        private String audience;
        @Value("${auth0.issuer}")
        private String issuer;

        @Override
        protected void configure(HttpSecurity httpSecurity) throws Exception {
            httpSecurity.cors();
            JwtWebSecurityConfigurer.forRS256(audience, issuer)
                    .configure(httpSecurity)
                    .authorizeRequests()
                    .antMatchers(URIConstants.VERSION).permitAll()
                    .antMatchers(URIConstants.STATISTICS).permitAll()
                    .anyRequest().authenticated();
        }

        @Override
        public void configure(WebSecurity web) throws Exception {
            // ignoring security for swagger APIs
            web.ignoring().antMatchers("/v3/api-docs", "/swagger-ui/**", "/v2/api-docs", "/configuration/ui", "/swagger-resources/**", "/configuration/security", "/swagger-ui.html", "/webjars/**", "/health").and()
                    .ignoring().antMatchers(HttpMethod.OPTIONS, "/**");
        }
    }
}
