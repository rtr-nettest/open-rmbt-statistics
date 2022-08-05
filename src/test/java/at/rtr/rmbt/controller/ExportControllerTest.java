package at.rtr.rmbt.controller;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.constant.URIConstants;
import at.rtr.rmbt.service.ExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(SpringExtension.class)
class ExportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ExportService exportService;

    @BeforeEach
    void setUp() {
        ExportController exportController = new ExportController(exportService);
        mockMvc = MockMvcBuilders.standaloneSetup(exportController)
                .build();
    }

    @Test
    void exportOpenData_correctInvocation_expectResponseEntity() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URIConstants.EXPORT_OPEN_DATA,
                        TestConstants.DEFAULT_YEAR, TestConstants.DEFAULT_MONTH, "csv"))
                .andDo(print());

        verify(exportService).exportOpenData(TestConstants.DEFAULT_YEAR,
                TestConstants.DEFAULT_MONTH,
                "csv",
                null);
    }

    @Test
    void exportOpenDataHours_correctInvocation_expectResponseEntity() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URIConstants.EXPORT_OPEN_DATA_HOURS,
                        TestConstants.DEFAULT_HOUR, "csv"))
                .andDo(print());

        verify(exportService).exportOpenData(null,
                null,
                "csv",
                TestConstants.DEFAULT_HOUR);
    }

    @Test
    void exportOpenDataRecent_correctInvocation_expectResponseEntity() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URIConstants.EXPORT_OPEN_DATA_RECENT, "csv"))
                .andDo(print());

        verify(exportService).exportOpenData(null,
                null,
                "csv",
                null);
    }
}