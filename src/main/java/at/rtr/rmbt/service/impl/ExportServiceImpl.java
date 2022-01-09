package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.service.ExportService;
import at.rtr.rmbt.service.export.opendata.CsvExportService;
import at.rtr.rmbt.service.export.opendata.XlsxExportService;
import at.rtr.rmbt.service.export.opendata.ZipExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {
    private static final boolean zip = true;
    private final ZipExportService zipExportService;
    private final CsvExportService csvExportService;
    private final XlsxExportService xlsxExportService;

    @Override
    public ResponseEntity<Object> exportOpenData(Integer year, Integer month, String format, Integer hour) {
        String tFormat = "csv";
        if (Objects.nonNull(format)) {
            tFormat = format;
        }
        final boolean xlsx = tFormat.contains("xlsx");
        if (xlsx) {
            return xlsxExportService.exportOpenData(year, month, hour);
        } else if (zip) {
            return zipExportService.exportOpenData(year, month, hour);
        } else {
            return csvExportService.exportOpenData(year, month, hour);
        }
    }
}
