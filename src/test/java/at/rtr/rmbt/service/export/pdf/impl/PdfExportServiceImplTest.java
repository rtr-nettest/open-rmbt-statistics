package at.rtr.rmbt.service.export.pdf.impl;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.TestUtils;
import at.rtr.rmbt.constant.Constants;
import at.rtr.rmbt.repository.OpenTestRepository;
import at.rtr.rmbt.response.OpenTestDetailsDTO;
import at.rtr.rmbt.response.opentest.OpenTestDTO;
import at.rtr.rmbt.response.opentest.OpenTestSearchResponse;
import at.rtr.rmbt.service.FileService;
import at.rtr.rmbt.service.UuidGenerator;
import at.rtr.rmbt.service.export.pdf.PdfGenerator;
import at.rtr.rmbt.utils.QueryParser;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class PdfExportServiceImplTest {


    @Mock
    private OpenTestRepository openTestRepository;
    @Mock
    private PdfGenerator pdfGenerator;
    @Mock
    private ResourceLoader resourceLoader;
    @Mock
    private UuidGenerator uuidGenerator;
    @Mock
    private Clock clock;
    @Mock
    private FileService fileService;
    @InjectMocks
    private PdfExportServiceImpl pdfExportService;

    @Mock
    private OpenTestSearchResponse openTestSearchResponse;
    @Mock
    private OpenTestDTO openTestDto;
    @Mock
    private OpenTestDetailsDTO openTestDetailsDTO;
    @Mock
    private Resource resource;

    @BeforeEach
    void setUp() {
        when(openTestDto.getOpenTestUuid()).thenReturn(TestConstants.DEFAULT_OPEN_TEST_UUID_STRING);
        when(uuidGenerator.generateNewUuid()).thenReturn(TestConstants.DEFAULT_UUID);
        when(clock.instant()).thenReturn(TestConstants.DEFAULT_INSTANT);
        ReflectionTestUtils.setField(pdfExportService, "pdfPath", TestConstants.DEFAULT_PDF_PATH);
    }

    @Test
    void generatePdf_emptyopenTestDtos_ResponseEntityNotFound() {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("open_test_uuid", TestConstants.DEFAULT_OPEN_TEST_UUID_STRING);

        when(openTestRepository.getOpenTestSearchResults(any(QueryParser.class), anyLong(), anyLong(), anySet()))
                .thenReturn(openTestSearchResponse);

        var actualResult = pdfExportService.generatePdf("application/json", parameters, null);

        assertEquals(expectedNotFoundResponse(), actualResult);
    }

    @Test
    void generatePdf_jsonAcceptHeader_ResponseEntity() throws IOException {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("open_test_uuid", TestConstants.DEFAULT_OPEN_TEST_UUID_STRING);
        when(openTestSearchResponse.getResults()).thenReturn(getOpenTestDtos());
        when(openTestRepository.getOpenTestByUuid(TestConstants.DEFAULT_OPEN_TEST_UUID_STRING)).thenReturn(openTestDetailsDTO);
        when(openTestRepository.getOpenTestSearchResults(any(QueryParser.class), anyLong(), anyLong(), anySet()))
                .thenReturn(openTestSearchResponse);
        when(resourceLoader.getResource(anyString())).thenReturn(resource);
        when(resource.getInputStream()).thenThrow(new IOException());

        var actualResult = pdfExportService.generatePdf("application/json", parameters, null);

        assertEquals(TestUtils.removeLineEndings(TestConstants.DEFAULT_EXPECTED_PDF_JSON_RESPONSE), TestUtils.removeLineEndings(actualResult.toString()));
    }

    @Test
    void loadPdf_correctInvocation_ResponseEntity() throws IOException {
        File mockFile = new File("src/test/resources/export/pdf/L7f2dfa9a-0755-4def-97a0-213f443793d5-20220116173631.pdf");
        when(fileService.openFile(TestConstants.DEFAULT_PDF_PATH + File.separator + TestConstants.DEFAULT_UUID + ".pdf")).thenReturn(mockFile);
        byte[] expectedBody = Files.readAllBytes(Paths.get("src/test/resources/export/pdf/L7f2dfa9a-0755-4def-97a0-213f443793d5-20220116173631.pdf"));

        var actualResult = pdfExportService.loadPdf(TestConstants.DEFAULT_FILE_NAME, TestConstants.DEFAULT_LANGUAGE);

        assertNotNull(actualResult.getBody());
        assertArrayEquals(expectedBody, (byte[]) actualResult.getBody());
        assertEquals(HttpStatus.OK, actualResult.getStatusCode());
        assertEquals(TestConstants.DEFAULT_PDF_RESPONSE_HEADERS, actualResult.getHeaders().toString());
    }

    @Test
    void loadPdf_fileNotExist_RuntimeException() {
        assertThrows(RuntimeException.class,
                () -> pdfExportService.loadPdf(TestConstants.DEFAULT_FILE_NAME, TestConstants.DEFAULT_LANGUAGE));
    }

    private List<OpenTestDTO> getOpenTestDtos() {
        return Lists.newArrayList(openTestDto);
    }

    private ResponseEntity<Object> expectedNotFoundResponse() {
        return ResponseEntity.notFound()
                .build();
    }
}