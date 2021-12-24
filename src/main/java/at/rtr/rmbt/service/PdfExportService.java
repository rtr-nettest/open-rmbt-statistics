package at.rtr.rmbt.service;

import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

public interface PdfExportService {

    ResponseEntity<Object> exportPdf(String acceptHeader, MultiValueMap<String, String> parameters);
}
