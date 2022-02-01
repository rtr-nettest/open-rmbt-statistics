package at.rtr.rmbt.controller;

import at.rtr.rmbt.constant.URIConstants;
import at.rtr.rmbt.service.ExportService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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

    @ApiOperation(value = "Export open data as CSV or XLSX",
            notes = "Bulk export open data entries",
            produces = "text/csv",
            nickname = "export")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "year", value = "Mandatory. The year that should be exported.", dataType = "string", example = "2017", paramType = "path", required = true),
            @ApiImplicitParam(name = "month", value = "Mandatory. The year that should be exported.", dataType = "integer", example = "0", paramType = "path", required = true),
            @ApiImplicitParam(name = "format", value = "Mandatory. Either ZIP (CSV) or XLSX.", dataType = "string", example = "xlsx", paramType = "path", required = true)
    })
    @GetMapping(URIConstants.EXPORT_OPEN_DATA)
    public ResponseEntity<Object> exportOpenData(@PathVariable int year, @PathVariable int month, @PathVariable String format) {
        return exportService.exportOpenData(year, month, format, null);
    }
}
