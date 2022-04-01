package at.rtr.rmbt.service.export.opendata;


import at.rtr.rmbt.dto.OpenTestExportResult;
import at.rtr.rmbt.mapper.OpenTestMapper;
import at.rtr.rmbt.repository.OpenTestExportRepository;
import at.rtr.rmbt.response.OpenTestExportDto;
import at.rtr.rmbt.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractExportService {

    private final OpenTestExportRepository openTestExportRepository;
    private final OpenTestMapper openTestMapper;
    private final FileService fileService;
    private static long cacheThresholdMs;

    public ResponseEntity<Object> exportOpenData(Integer year, Integer month, Integer hour) {
        //Before doing anything => check if a cached file already exists and is new enough
        //allow filtering by month/year
        boolean hoursExport = false;
        boolean dateExport = false;

        if (Objects.nonNull(hour)) { // export by hours
            if (hour <= 7 * 24 && hour >= 1) {  //limit to 1 week (avoid DoS)
                hoursExport = true;
            }
        } else if (Objects.nonNull(year) && Objects.nonNull(month)) {  // export by month/year
            if (year < 2099 && month > 0 && month <= 12 && year > 2000) {
                dateExport = true;
            }
        }

        String property = System.getProperty("java.io.tmpdir");
        final String filename;

        if (hoursExport) {
            filename = getFileNameHours().replace("%HOURS%", String.format("%03d", hour));
            cacheThresholdMs = 5 * 60 * 1000; //5 minutes
        } else if (dateExport) {
            filename = getFileName().replace("%YEAR%", Integer.toString(year)).replace("%MONTH%", String.format("%02d", month));
            cacheThresholdMs = 23 * 60 * 60 * 1000; //23 hours
        } else {
            filename = getFileNameCurrent();
            cacheThresholdMs = 3 * 60 * 60 * 1000; //3 hours
        }

        MediaType mediaType = getMediaType();

        ResponseEntity.BodyBuilder responseEntity = ResponseEntity.ok()
                .contentType(mediaType);
        setContentDisposition(responseEntity, filename);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            final File cachedFile = fileService.openFile(property + File.separator + filename);
            final File generatingFile = fileService.openFile(property + File.separator + filename + "_tmp");
            if (cachedFile.exists()) {
                log.info("Cache file " + cachedFile + " exists");
                //check if file has been recently created OR a file is currently being created
                if (((cachedFile.lastModified() + cacheThresholdMs) > (new Date()).getTime()) ||
                        (generatingFile.exists() && (generatingFile.lastModified() + cacheThresholdMs) > (new Date()).getTime())) {
                    log.info("Read file from cache " + cachedFile);
                    //if so, return the cached file instead of a cost-intensive new one
                    InputStream is = fileService.getFileInputStream(cachedFile);
                    IOUtils.copy(is, out);

                    return responseEntity
                            .body(out.toByteArray());

                }
            }

            final List<OpenTestExportDto> results = getOpenTestExportDtos(year, month, hour, hoursExport, dateExport);
            writeNewFile(out, results, filename);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

        return responseEntity.body(out.toByteArray());
    }

    protected void writeNewFile(OutputStream out, List<OpenTestExportDto> results, String fileName) throws IOException {
        log.info("Creating new file " + fileName);
        //cache in file => create temporary temporary file (to
        // handle errors while fulfilling a request)
        String property = System.getProperty("java.io.tmpdir");
        final File cachedFile = new File(property + File.separator + fileName + "_tmp");
        OutputStream outf = new FileOutputStream(cachedFile);

        //custom logic
        writeCustomLogic(results, outf, fileName);
        //end custom logic

        //if we reach this code, the data is now cached in a temporary tmp-file
        //so, rename the file for "production use2
        //concurrency issues should be solved by the operating system
        File newCacheFile = new File(property + File.separator + fileName);
        Files.move(cachedFile.toPath(), newCacheFile.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);

        FileInputStream fis = new FileInputStream(newCacheFile);
        IOUtils.copy(fis, out);
        fis.close();
        out.close();
    }

    protected abstract void writeCustomLogic(List<OpenTestExportDto> results, OutputStream out, String fileName) throws IOException;

    protected abstract MediaType getMediaType();

    protected abstract String getFileNameHours();

    protected abstract String getFileName();

    protected abstract String getFileNameCurrent();

    protected void setContentDisposition(ResponseEntity.BodyBuilder responseEntity, String filename) {

    }

    private List<OpenTestExportDto> getOpenTestExportDtos(Integer year, Integer month, Integer hour, boolean hoursExport, boolean dateExport) {
        List<OpenTestExportResult> exportResults;
        if (hoursExport) {
            exportResults = openTestExportRepository.getOpenTestExportHour(hour);
        } else if (dateExport) {
            exportResults = openTestExportRepository.getOpenTestExportMonth(year, month);
        } else {
            exportResults = openTestExportRepository.getOpenTestExportLast31Days();
        }
        log.info("Sent request to database");
        return exportResults.stream()
                .map(openTestMapper::openTestExportResultToOpenTestExportDto)
                .collect(Collectors.toList());
    }
}
