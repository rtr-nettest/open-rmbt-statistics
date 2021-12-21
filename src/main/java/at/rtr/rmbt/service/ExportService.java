package at.rtr.rmbt.service;

import org.springframework.http.ResponseEntity;

public interface ExportService {

    ResponseEntity<Object> exportOpenData(Integer year, Integer month, String format, Integer hour);
}
