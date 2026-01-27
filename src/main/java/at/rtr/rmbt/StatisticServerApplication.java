package at.rtr.rmbt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@PropertySource({"classpath:git.properties"})
@EnableCaching
@EnableScheduling
@ConfigurationPropertiesScan
public class StatisticServerApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(StatisticServerApplication.class, args);
    }


    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(StatisticServerApplication.class);
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:SystemMessages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
