package at.rtr.rmbt.controller;

import at.rtr.rmbt.constant.URIConstants;
import at.rtr.rmbt.service.export.pdf.PdfExportService;
import at.rtr.rmbt.utils.ControllerUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

@RestController
@Slf4j
@RequiredArgsConstructor
@MultipartConfig
public class PdfExportController {

    private final PdfExportService pdfExportService;

    @Operation(summary = "Export open data as PDF",
            responses = @ApiResponse(content = @Content(mediaType = "application/pdf")),
            operationId = "exportPdf",
            parameters = {
                    @Parameter(name = "open_test_uuid",
                            description = "The UUID of the test.",
                            schema = @Schema(type = "string"),
                            in = ParameterIn.QUERY),
                    @Parameter(name = "loop_uuid",
                            description = "The loop UUID of a single loop test",
                            schema = @Schema(type = "string"),
                            in = ParameterIn.QUERY)
            })
    @GetMapping(URIConstants.EXPORT_PDF_FILENAME)
    public ResponseEntity<Object> getExportPdf(@PathVariable String fileName) {
        return pdfExportService.loadPdf(fileName, null);
    }

    @Operation(summary = "Export open data as PDF",
            responses = @ApiResponse(content = @Content(mediaType = "application/pdf")),
            operationId = "exportPdf",
            parameters = {
                    @Parameter(name = "open_test_uuid",
                            description = "The UUID of the test.",
                            schema = @Schema(type = "string"),
                            in = ParameterIn.QUERY),
                    @Parameter(name = "loop_uuid",
                            description = "The loop UUID of a single loop test",
                            schema = @Schema(type = "string"),
                            in = ParameterIn.QUERY)
            })
    @GetMapping(URIConstants.EXPORT_PDF_LANG_FILENAME)
    public ResponseEntity<Object> getExportPdfLang(@PathVariable String fileName, @PathVariable String lang) {
        return pdfExportService.loadPdf(fileName, lang);
    }

    @Operation(summary = "Export open data as PDF",
            responses = @ApiResponse(content = @Content(mediaType = "application/pdf")),
            operationId = "exportPdf",
            parameters = {
                    @Parameter(name = "open_test_uuid",
                            description = "The UUID of the test.",
                            schema = @Schema(type = "string"),
                            in = ParameterIn.QUERY),
                    @Parameter(name = "loop_uuid",
                            description = "The loop UUID of a single loop test",
                            schema = @Schema(type = "string"),
                            in = ParameterIn.QUERY)
            })
    @PostMapping(URIConstants.EXPORT_PDF)
    public ResponseEntity<Object> postExportPdf(@RequestHeader("accept") String acceptHeader,
                                                @Parameter(hidden = true) @RequestParam MultiValueMap<String, String> parameters,
                                                HttpServletRequest request) throws ServletException, IOException {
        //handle multipart forms
        if (request.getParts().size() > 1) {
            ControllerUtils.addParametersFromMultipartRequest(parameters, request);
        }
        return pdfExportService.generatePdf(acceptHeader, parameters, null);
    }

    @Operation(summary = "Export open data as PDF",
            responses = @ApiResponse(content = @Content(mediaType = "application/pdf")),
            operationId = "exportPdf",
            parameters = {
                    @Parameter(name = "open_test_uuid",
                            description = "The UUID of the test.",
                            schema = @Schema(type = "string"),
                            in = ParameterIn.QUERY),
                    @Parameter(name = "loop_uuid",
                            description = "The loop UUID of a single loop test",
                            schema = @Schema(type = "string"),
                            in = ParameterIn.QUERY)
            })
    @PostMapping(URIConstants.EXPORT_PDF_LANG)
    public ResponseEntity<Object> postExportPdfLang(@PathVariable String lang,
                                                    @RequestHeader("accept") String acceptHeader,
                                                    @Parameter(hidden = true) @RequestParam MultiValueMap<String, String> parameters,
                                                    HttpServletRequest request) throws ServletException, IOException {
        //handle non-multipart form
        if (request.getContentType().toLowerCase().startsWith("multipart")) {
            //handle multipart forms
            if (request.getParts().size() > 1) {
                ControllerUtils.addParametersFromMultipartRequest(parameters, request);
            }
        }
        return pdfExportService.generatePdf(acceptHeader, parameters, lang);
    }
}
