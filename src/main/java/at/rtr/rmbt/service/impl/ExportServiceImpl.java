package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.mapper.OpenTestMapper;
import at.rtr.rmbt.repository.OpenTestExportRepository;
import at.rtr.rmbt.response.OpenTestExportDto;
import at.rtr.rmbt.service.ExportService;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.github.sett4.dataformat.xlsx.XlsxMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.util.IOUtils;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {
    private static final String FILENAME_CSV_HOURS = "netztest-opendata_hours-%HOURS%.csv";
    private static final String FILENAME_ZIP_HOURS = "netztest-opendata_hours-%HOURS%.zip";
    private static final String FILENAME_XLSX_HOURS = "netztest-opendata_hours-%HOURS%.xlsx";
    private static final String FILENAME_CSV = "netztest-opendata-%YEAR%-%MONTH%.csv";
    private static final String FILENAME_XLSX = "netztest-opendata-%YEAR%-%MONTH%.xlsx";
    private static final String FILENAME_ZIP = "netztest-opendata-%YEAR%-%MONTH%.zip";
    private static final String FILENAME_CSV_CURRENT = "netztest-opendata.csv";
    private static final String FILENAME_ZIP_CURRENT = "netztest-opendata.zip";
    private static final String FILENAME_XLSX_CURRENT = "netztest-opendata.xlsx";

    private static final boolean zip = true;

    private static long cacheThresholdMs;

    private final OpenTestExportRepository openTestExportRepository;
    private final ResourceLoader resourceLoader;
    private final OpenTestMapper openTestMapper;

    @Override
    public ResponseEntity<Object> exportOpenData(Integer year, Integer month, String format, Integer hour) {
        //Before doing anything => check if a cached file already exists and is new enough
        String property = System.getProperty("java.io.tmpdir");

        final String filename_zip;
        final String filename_csv;
        final String filename_xlsx;

        //allow filtering by month/year
        boolean hoursExport = false;
        boolean dateExport = false;

        String tFormat = "csv";
        if (Objects.nonNull(format)) {
            tFormat = format;
        }
        final boolean xlsx = tFormat.contains("xlsx");

        if (Objects.nonNull(hour)) { // export by hours
            if (hour <= 7 * 24 && hour >= 1) {  //limit to 1 week (avoid DoS)
                hoursExport = true;
            }
        } else if (Objects.nonNull(year) && Objects.nonNull(month)) {  // export by month/year
            if (year < 2099 && month > 0 && month <= 12 && year > 2000) {
                dateExport = true;
            }
        }

        if (hoursExport) {
            filename_zip = FILENAME_ZIP_HOURS.replace("%HOURS%", String.format("%03d", hour));
            filename_csv = FILENAME_CSV_HOURS.replace("%HOURS%", String.format("%03d", hour));
            filename_xlsx = FILENAME_XLSX_HOURS.replace("%HOURS%", String.format("%03d", hour));
            cacheThresholdMs = 5 * 60 * 1000; //5 minutes
        } else if (dateExport) {
            filename_zip = FILENAME_ZIP.replace("%YEAR%", Integer.toString(year)).replace("%MONTH%", String.format("%02d", month));
            filename_csv = FILENAME_CSV.replace("%YEAR%", Integer.toString(year)).replace("%MONTH%", String.format("%02d", month));
            filename_xlsx = FILENAME_XLSX.replace("%YEAR%", Integer.toString(year)).replace("%MONTH%", String.format("%02d", month));
            cacheThresholdMs = 23 * 60 * 60 * 1000; //23 hours
        } else {
            filename_zip = FILENAME_ZIP_CURRENT;
            filename_csv = FILENAME_CSV_CURRENT;
            filename_xlsx = FILENAME_XLSX_CURRENT;
            cacheThresholdMs = 3 * 60 * 60 * 1000; //3 hours
        }
        final String filename = ((xlsx) ? filename_xlsx : (zip) ? filename_zip : filename_csv);


        MediaType mediaType = getMediaType(xlsx);
        ResponseEntity.BodyBuilder responseEntity = ResponseEntity.ok()
                .contentType(mediaType);
        if (xlsx || zip) {
            responseEntity
                    .header("Content-Disposition", "attachment; filename=" + filename);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            final File cachedFile = new File(property + File.separator + filename);
            final File generatingFile = new File(property + File.separator + filename + "_tmp");
            if (cachedFile.exists()) {
                //check if file has been recently created OR a file is currently being created
                if (((cachedFile.lastModified() + cacheThresholdMs) > (new Date()).getTime()) ||
                        (generatingFile.exists() && (generatingFile.lastModified() + cacheThresholdMs) > (new Date()).getTime())) {

                    //if so, return the cached file instead of a cost-intensive new one
                    InputStream is = new FileInputStream(cachedFile);
                    IOUtils.copy(is, out);

                    return responseEntity
                            .body(out.toByteArray());

                }
            }

            final List<OpenTestExportDto> results = openTestExportRepository.getOpenTestExport(hoursExport, dateExport, year, month, hour).stream()
                    .map(openTestMapper::openTestExportResultToOpenTestExportDto)
                    .collect(Collectors.toList());
            writeNewFile(out, results, filename, xlsx, filename_csv);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

        return responseEntity.body(out.toByteArray());
    }

    private MediaType getMediaType(boolean xlsx) {
        return xlsx ?
                new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet") :
                zip ? new MediaType("application", "zip")
                        : new MediaType("text", "csv");
    }

    public void writeNewFile(OutputStream out, List<OpenTestExportDto> results, String filename, boolean xlsx, String filename_csv) throws IOException {
        //cache in file => create temporary temporary file (to
        // handle errors while fulfilling a request)
        String property = System.getProperty("java.io.tmpdir");
        final File cachedFile = new File(property + File.separator + filename + "_tmp");
        OutputStream outf = new FileOutputStream(cachedFile);

        if (xlsx) {
            XlsxMapper mapper = new XlsxMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            CsvSchema schema = mapper.schemaFor(OpenTestExportDto.class).withHeader();
            SequenceWriter sequenceWriter = mapper.writer(schema).writeValues(outf);
            sequenceWriter.writeAll(results);
            sequenceWriter.flush();
            sequenceWriter.close();
        } else {
            if (zip) {
                final ZipOutputStream zos = new ZipOutputStream(outf);
                final ZipEntry zeLicense = new ZipEntry("LIZENZ.txt");
                zos.putNextEntry(zeLicense);
                final InputStream licenseIS = resourceLoader.getResource("classpath:png/DATA_LICENSE.txt").getInputStream();
                IOUtils.copy(licenseIS, zos);
                licenseIS.close();

                final ZipEntry zeCsv = new ZipEntry(filename_csv);
                zos.putNextEntry(zeCsv);
                outf = zos;
            }

            final CsvMapper cm = new CsvMapper();
            final CsvSchema schema;
            cm.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            cm.enable(CsvGenerator.Feature.STRICT_CHECK_FOR_QUOTING);
            schema = CsvSchema.builder().setLineSeparator("\r\n").setUseHeader(true)
                    .addColumnsFrom(cm.schemaFor(OpenTestExportDto.class)).build();
            cm.writer(schema).writeValue(outf, results);

            if (zip)
                outf.close();


        }
        //if we reach this code, the data is now cached in a temporary tmp-file
        //so, rename the file for "production use2
        //concurrency issues should be solved by the operating system
        File newCacheFile = new File(property + File.separator + filename);
        Files.move(cachedFile.toPath(), newCacheFile.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);

        FileInputStream fis = new FileInputStream(newCacheFile);
        IOUtils.copy(fis, out);
        fis.close();
        out.close();
    }
}
