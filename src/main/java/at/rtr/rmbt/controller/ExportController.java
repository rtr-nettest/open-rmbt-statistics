package at.rtr.rmbt.controller;

import at.rtr.rmbt.constant.URIConstants;
import at.rtr.rmbt.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @Operation(summary = "Export open data as CSV or XLSX",
            description = "Bulk export open data entries",
            responses = @ApiResponse(content = @Content(mediaType = "text/csv")),
            operationId = "export",
            parameters = {
                    @Parameter(name = "year", description = "Mandatory. The year that should be exported.", example = "2017", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "month", description = "Mandatory. The year that should be exported.", example = "0", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "format", description = "Mandatory. Either ZIP (CSV) or XLSX.", example = "xlsx", in = ParameterIn.PATH, required = true)
            })
    @GetMapping(URIConstants.EXPORT_OPEN_DATA)
    public ResponseEntity<Object> exportOpenData(@PathVariable int year, @PathVariable int month, @PathVariable String format) {
        return exportService.exportOpenData(year, month, format, null);
    }

    @Operation(summary = "Export open data as CSV or XLSX",
            description = "Bulk export open data entries",
            responses = @ApiResponse(content = @Content(mediaType = "text/csv")),
            operationId = "export",
            parameters = {
                    @Parameter(name = "hours", description = "Mandatory. The year that should be exported.", example = "2017", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "format", description = "Mandatory. Either ZIP (CSV) or XLSX.", example = "xlsx", in = ParameterIn.PATH, required = true)
            })
    @GetMapping(URIConstants.EXPORT_OPEN_DATA_HOURS)
    public ResponseEntity<Object> exportOpenDataHours(@PathVariable int hours, @PathVariable String format) {
        return exportService.exportOpenData(null, null, format, hours);
    }

    @Operation(summary = "Export open data as CSV or XLSX",
            description = "Bulk export open data entries",
            responses = @ApiResponse(content = @Content(mediaType = "text/csv")),
            operationId = "export",
            parameters = {
                    @Parameter(name = "format", description = "Mandatory. Either ZIP (CSV) or XLSX.", example = "xlsx", in = ParameterIn.PATH, required = true)
            })
    @GetMapping(URIConstants.EXPORT_OPEN_DATA_RECENT)
    public ResponseEntity<Object> exportOpenDataRecent(@PathVariable String format) {
        return exportService.exportOpenData(null, null, format, null);
    }
}
