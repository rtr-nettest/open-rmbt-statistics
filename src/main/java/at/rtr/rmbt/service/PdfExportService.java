package at.rtr.rmbt.service;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface PdfExportService {

    ResponseEntity<Object> exportPdf(String acceptHeader, Map<String, List<String>> parameters);
}
