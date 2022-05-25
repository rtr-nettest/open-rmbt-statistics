package at.rtr.rmbt.onstartup;

import at.rtr.rmbt.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class OnStartUpRunner implements ApplicationRunner {

    @Value("${app.fileCache.pdfPath}")
    private String fileCachePdfPath;

    @Value("${app.fileCache.path}")
    private String fileCachePath;

    @Value("${app.fileCache.expirationTerm}")
    private Integer fileCacheExpirationTerm;

    private final FileService fileService;
    private final Clock clock;


    @Scheduled(fixedRateString = "${app.fileCache.cleaningJobRate}")
    public void clearFileCache() {
        log.info("Clear file cache job started");
        File dir = fileService.openFile(fileCachePath);
        if (dir.exists() && Objects.nonNull(dir.listFiles())) {
            Instant staleInstant = clock.instant().minus(fileCacheExpirationTerm, ChronoUnit.HOURS);
            for (File file : dir.listFiles()) {
                Instant lastModifiedInstant = Instant.ofEpochMilli(file.lastModified());
                if (lastModifiedInstant.isBefore(staleInstant)) {
                    file.delete();
                    log.info("File {} is deleted from directory {}", file.getAbsolutePath(), dir.getAbsolutePath());
                }
            }
        } else {
            log.error("Temp directory {} does not exists", dir.getAbsolutePath());
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        createTempExportDirIfNotExist(fileCachePath);
        createTempExportDirIfNotExist(fileCachePdfPath);

    }

    private void createTempExportDirIfNotExist(String path) {
        File tempDir = fileService.openFile(path);
        if (!tempDir.exists()) {
            boolean isTempDirectoryCreatedSuccessfully = tempDir.mkdirs();
            if (isTempDirectoryCreatedSuccessfully) {
                log.info("Temp directory {} is created successfully", tempDir.getAbsolutePath());
            }
        }
    }
}
