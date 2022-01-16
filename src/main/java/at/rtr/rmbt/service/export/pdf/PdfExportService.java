package at.rtr.rmbt.service.export.pdf;

import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

public interface PdfExportService {

    ResponseEntity<Object> generatePdf(String acceptHeader, MultiValueMap<String, String> parameters, String lang);

    ResponseEntity<Object> loadPdf(String fileName, String lang);
}
