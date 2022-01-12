package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.service.export.opendata.CsvExportService;
import at.rtr.rmbt.service.export.opendata.XlsxExportService;
import at.rtr.rmbt.service.export.opendata.ZipExportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ExportServiceImplTest {

    @Mock
    private ZipExportService zipExportService;
    @Mock
    private CsvExportService csvExportService;
    @Mock
    private XlsxExportService xlsxExportService;
    @InjectMocks
    private ExportServiceImpl exportService;

    @Mock
    private ResponseEntity<Object> responseEntity;

    @Test
    void exportOpenData_xlsx_ResponseEntity() {
        when(xlsxExportService.exportOpenData(TestConstants.DEFAULT_YEAR, TestConstants.DEFAULT_MONTH, TestConstants.DEFAULT_HOUR))
                .thenReturn(responseEntity);

        var actualResult = exportService.exportOpenData(TestConstants.DEFAULT_YEAR, TestConstants.DEFAULT_MONTH, "xlsx", TestConstants.DEFAULT_HOUR);

        assertEquals(responseEntity, actualResult);
    }

    @Test
    void exportOpenData_zip_ResponseEntity(){
        when(zipExportService.exportOpenData(TestConstants.DEFAULT_YEAR, TestConstants.DEFAULT_MONTH, TestConstants.DEFAULT_HOUR))
                .thenReturn(responseEntity);

        var actualResult = exportService.exportOpenData(TestConstants.DEFAULT_YEAR, TestConstants.DEFAULT_MONTH, "csv", TestConstants.DEFAULT_HOUR);

        assertEquals(responseEntity, actualResult);
    }
}