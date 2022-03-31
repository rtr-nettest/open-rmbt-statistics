package at.rtr.rmbt.service.export.opendata;

import at.rtr.rmbt.mapper.OpenTestMapper;
import at.rtr.rmbt.repository.OpenTestExportRepository;
import at.rtr.rmbt.response.OpenTestExportDto;
import at.rtr.rmbt.service.FileService;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.github.sett4.dataformat.xlsx.XlsxMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Service
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
