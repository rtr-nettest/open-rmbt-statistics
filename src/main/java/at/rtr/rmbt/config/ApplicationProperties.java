package at.rtr.rmbt.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("app")
public class ApplicationProperties {

    private String defaultLanguage;

    private FileCache fileCache;

    @Getter
    @Setter
    public static class FileCache {
        private String path;
        private String cleaningJobRate;
        private Integer expirationTerm;
    }
}
