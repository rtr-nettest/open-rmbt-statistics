package at.rtr.rmbt.controller;

import at.rtr.rmbt.constant.URIConstants;
import at.rtr.rmbt.service.export.pdf.PdfExportService;
import at.rtr.rmbt.utils.ControllerUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@Slf4j
@RequiredArgsConstructor
public class PdfExportController {

    private final PdfExportService pdfExportService;

    @ApiOperation(value = "Export open data as PDF",
            produces = "application/pdf",
            nickname = "exportPdf")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "open_test_uuid", value = "The UUID of the test.", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "loop_uuid", value = "The loop UUID of a single loop test", dataType = "string", paramType = "query")
    })
    @GetMapping(URIConstants.EXPORT_PDF_FILENAME)
    public ResponseEntity<Object> getExportPdf(@PathVariable String fileName) {
        return pdfExportService.loadPdf(fileName, null);
    }

    @ApiOperation(value = "Export open data as PDF",
            produces = "application/pdf",
            nickname = "exportPdf")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "open_test_uuid", value = "The UUID of the test.", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "loop_uuid", value = "The loop UUID of a single loop test", dataType = "string", paramType = "query")
    })
    @GetMapping(URIConstants.EXPORT_PDF_LANG_FILENAME)
    public ResponseEntity<Object> getExportPdfLang(@PathVariable String fileName, @PathVariable String lang) {
        return pdfExportService.loadPdf(fileName, lang);
    }

    @ApiOperation(value = "Export open data as PDF",
            produces = "application/pdf",
            nickname = "exportPdf")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "open_test_uuid", value = "The UUID of the test.", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "loop_uuid", value = "The loop UUID of a single loop test", dataType = "string", paramType = "query")
    })
    @PostMapping(URIConstants.EXPORT_PDF)
    public ResponseEntity<Object> postExportPdf(@RequestHeader("accept") String acceptHeader,
                                                @RequestParam MultiValueMap<String, String> parameters,
                                                HttpServletRequest request) {
        //handle multipart forms
        if (ServletFileUpload.isMultipartContent(request)) {
            ControllerUtils.addParametersFromMultipartRequest(parameters, request);
        }
        return pdfExportService.generatePdf(acceptHeader, parameters, null);
    }

    @ApiOperation(value = "Export open data as PDF",
            produces = "application/pdf",
            nickname = "exportPdf")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "open_test_uuid", value = "The UUID of the test.", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "loop_uuid", value = "The loop UUID of a single loop test", dataType = "string", paramType = "query")
    })
    @PostMapping(URIConstants.EXPORT_PDF_LANG)
    public ResponseEntity<Object> postExportPdfLang(@PathVariable String lang,
                                                    @RequestHeader("accept") String acceptHeader,
                                                    @RequestParam MultiValueMap<String, String> parameters,
                                                    HttpServletRequest request) {
        //handle multipart forms
        if (ServletFileUpload.isMultipartContent(request)) {
            ControllerUtils.addParametersFromMultipartRequest(parameters, request);
        }
        return pdfExportService.generatePdf(acceptHeader, parameters, lang);
    }
}
