package at.rtr.rmbt.service.export.opendata;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.mapper.OpenTestMapper;
import at.rtr.rmbt.repository.OpenTestExportRepository;
import at.rtr.rmbt.response.OpenTestExportDto;
import at.rtr.rmbt.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.*;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class AbstractExportServiceTest {

    @Mock
    private OpenTestExportRepository openTestExportRepository;
    @Mock
    private OpenTestMapper openTestMapper;
    @Mock
    private FileService fileService;
    @Mock
    private File cachedFile;
    @Mock
    private File generatingFile;

    private AbstractExportService exportService;

    @BeforeEach
    void setUp() {
        exportService = new AbstractExportService(openTestExportRepository, openTestMapper, fileService) {
            @Override
            protected void writeCustomLogic(List<OpenTestExportDto> results, OutputStream out, String fileName) throws IOException {

            }

            @Override
            protected MediaType getMediaType() {
                return TestConstants.DEFAULT_MEDIA_TYPE;
            }

            @Override
            protected String getFileNameHours() {
                return null;
            }

            @Override
            protected String getFileName() {
                return TestConstants.DEFAULT_FILE_NAME;
            }

            @Override
            protected String getFileNameCurrent() {
                return null;
            }
        };
        ReflectionTestUtils.setField(exportService, "fileCachePath", TestConstants.DEFAULT_FILE_CACHE_PATH);
    }

    @Test
    void exportOpenData_correctInvocation_fileExists() throws FileNotFoundException {
        when(fileService.openFile(TestConstants.DEFAULT_FILE_CACHE_PATH + File.separator + TestConstants.DEFAULT_FILE_NAME))
                .thenReturn(cachedFile);
        when(fileService.openFile(TestConstants.DEFAULT_FILE_CACHE_PATH + File.separator + TestConstants.DEFAULT_FILE_NAME + "_tmp"))
                .thenReturn(generatingFile);
        when(cachedFile.lastModified()).thenReturn(new Date().getTime());
        when(cachedFile.exists()).thenReturn(true);
        when(fileService.getFileInputStream(cachedFile)).thenReturn(new FileInputStream(new File("src/test/resources/opendata/netztest-opendata-2021-11.xlsx")));

        exportService.exportOpenData(TestConstants.DEFAULT_YEAR, TestConstants.DEFAULT_MONTH, null);

        verifyNoInteractions(openTestExportRepository);
    }
}