package at.rtr.rmbt.service.export.opendata;

import at.rtr.rmbt.mapper.OpenTestMapper;
import at.rtr.rmbt.repository.OpenTestExportRepository;
import at.rtr.rmbt.response.OpenTestExportDto;
import org.apache.poi.util.IOUtils;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ZipExportService extends CsvExportService {
    private static final String FILENAME_ZIP_HOURS = "netztest-opendata_hours-%HOURS%.zip";
    private static final String FILENAME_ZIP = "netztest-opendata-%YEAR%-%MONTH%.zip";
    private static final String FILENAME_ZIP_CURRENT = "netztest-opendata.zip";

    private final ResourceLoader resourceLoader;

    public ZipExportService(ResourceLoader resourceLoader,
                            OpenTestExportRepository openTestExportRepository,
                            OpenTestMapper openTestMapper) {
        super(openTestExportRepository, openTestMapper);
        this.resourceLoader = resourceLoader;
    }

    @Override
    protected void writeCustomLogic(List<OpenTestExportDto> results, OutputStream out, String fileName) throws IOException {
        final ZipOutputStream zos = new ZipOutputStream(out);
        final ZipEntry zeLicense = new ZipEntry("LIZENZ.txt");
        zos.putNextEntry(zeLicense);
        final InputStream licenseIS = resourceLoader.getResource("classpath:png/DATA_LICENSE.txt").getInputStream();
        IOUtils.copy(licenseIS, zos);
        licenseIS.close();

        final ZipEntry zeCsv = new ZipEntry(fileName.replace("zip", "csv"));
        zos.putNextEntry(zeCsv);
        out = zos;
        super.writeCustomLogic(results, out, fileName);
        out.close();
    }

    @Override
    protected MediaType getMediaType() {
        return new MediaType("text", "csv");
    }

    @Override
    protected String getFileNameHours() {
        return FILENAME_ZIP_HOURS;
    }

    @Override
    protected String getFileName() {
        return FILENAME_ZIP;
    }

    @Override
    protected String getFileNameCurrent() {
        return FILENAME_ZIP_CURRENT;
    }
}
