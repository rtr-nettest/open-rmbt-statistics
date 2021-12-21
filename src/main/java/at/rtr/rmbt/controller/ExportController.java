package at.rtr.rmbt.controller;

import at.rtr.rmbt.constant.URIConstants;
import at.rtr.rmbt.service.ExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @GetMapping(URIConstants.EXPORT_OPEN_DATA)
    public ResponseEntity<Object> exportOpenData(@PathVariable int year, @PathVariable int month, @PathVariable String format) {
        return exportService.exportOpenData(year, month, format, null);
    }
}
