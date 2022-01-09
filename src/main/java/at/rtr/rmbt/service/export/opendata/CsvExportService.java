package at.rtr.rmbt.service.export.opendata;

import at.rtr.rmbt.mapper.OpenTestMapper;
import at.rtr.rmbt.repository.OpenTestExportRepository;
import at.rtr.rmbt.response.OpenTestExportDto;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Service
public class CsvExportService extends AbstractExportService {
    private static final String FILENAME_CSV_HOURS = "netztest-opendata_hours-%HOURS%.csv";
    private static final String FILENAME_CSV = "netztest-opendata-%YEAR%-%MONTH%.csv";
    private static final String FILENAME_CSV_CURRENT = "netztest-opendata.csv";

    public CsvExportService(OpenTestExportRepository openTestExportRepository, OpenTestMapper openTestMapper) {
        super(openTestExportRepository, openTestMapper);
    }

    @Override
    protected void writeCustomLogic(List<OpenTestExportDto> results, OutputStream out, String fileName) throws IOException {
        final CsvMapper cm = new CsvMapper();
        final CsvSchema schema;
        cm.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        cm.enable(CsvGenerator.Feature.STRICT_CHECK_FOR_QUOTING);
        schema = CsvSchema.builder().setLineSeparator("\r\n").setUseHeader(true)
                .addColumnsFrom(cm.schemaFor(OpenTestExportDto.class)).build();
        cm.writer(schema).writeValue(out, results);
    }

    @Override
    protected MediaType getMediaType() {
        return new MediaType("text", "csv");
    }

    @Override
    protected String getFileNameHours() {
        return FILENAME_CSV_HOURS;
    }

    @Override
    protected String getFileName() {
        return FILENAME_CSV;
    }

    @Override
    protected String getFileNameCurrent() {
        return FILENAME_CSV_CURRENT;
    }
}
