package at.rtr.rmbt.service.export.opendata;

import at.rtr.rmbt.mapper.OpenTestMapper;
import at.rtr.rmbt.repository.OpenTestExportRepository;
import at.rtr.rmbt.response.OpenTestExportDto;
import at.rtr.rmbt.service.FileService;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.github.sett4.dataformat.xlsx.XlsxMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.DefaultTempFileCreationStrategy;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Service
@Slf4j
public class XlsxExportService extends AbstractExportService {

    private static final String FILENAME_XLSX_HOURS = "netztest-opendata_hours-%HOURS%.xlsx";
    private static final String FILENAME_XLSX = "netztest-opendata-%YEAR%-%MONTH%.xlsx";
    private static final String FILENAME_XLSX_CURRENT = "netztest-opendata.xlsx";

    public XlsxExportService(OpenTestExportRepository openTestExportRepository,
                             OpenTestMapper openTestMapper,
                             FileService fileService) {
        super(openTestExportRepository,
                openTestMapper,
                fileService);
    }

    protected void writeCustomLogic(List<OpenTestExportDto> results, OutputStream outf, String fileName) throws IOException {
        XlsxMapper mapper = new XlsxMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        CsvSchema schema = mapper.schemaFor(OpenTestExportDto.class).withHeader();
        SequenceWriter sequenceWriter = mapper.writer(schema).writeValues(outf);
        sequenceWriter.writeAll(results);
        sequenceWriter.flush();
        sequenceWriter.close();

        File poiFilesTmpDir = new File(System.getProperty("java.io.tmpdir"), "poifiles");
        if (poiFilesTmpDir.exists() &&  poiFilesTmpDir.isDirectory()) {
            deleteOldFiles(poiFilesTmpDir.toPath());
        }

    }

    private static void deleteOldFiles(Path directory) {
        Instant cutoffTimestamp = Instant.now().minus(1, ChronoUnit.HOURS);
        try (Stream<Path> files = Files.list(directory)) {
            files.filter(Files::isRegularFile)
                    .filter(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toInstant().compareTo(cutoffTimestamp) < 0;
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            log.info("deleting old poi file " + path);
                            Files.delete(path);
                        } catch (IOException e) {
                            log.error("error deleting poi file " + path);
                        }
                    });
        } catch (IOException e) {}
    }

    protected MediaType getMediaType() {
        return new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }


    protected String getFileNameHours() {
        return FILENAME_XLSX_HOURS;
    }


    protected String getFileName() {
        return FILENAME_XLSX;
    }

    protected String getFileNameCurrent() {
        return FILENAME_XLSX_CURRENT;
    }

    @Override
    protected void setContentDisposition(ResponseEntity.BodyBuilder responseEntity, String filename) {
        responseEntity
                .header("Content-Disposition", "attachment; filename=" + filename);
    }
}
