package at.rtr.rmbt.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Registers the {@link MonitoringAuthFilter} in front of the JavaMelody report. Ordered ahead of
 * everything else so it gates {@code /monitoring} before JavaMelody's own filter serves it.
 */
@Component
public class MonitoringAuthFilterConfig {

    @Bean
    public FilterRegistrationBean<MonitoringAuthFilter> monitoringAuthFilter(Environment environment) {
        FilterRegistrationBean<MonitoringAuthFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new MonitoringAuthFilter(environment));
        registrationBean.setName("monitoringAuthFilter");
        registrationBean.addUrlPatterns("/monitoring", "/monitoring/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }
}
