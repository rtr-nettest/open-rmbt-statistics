package at.rtr.rmbt.controller;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.TestObjects;
import at.rtr.rmbt.constant.URIConstants;
import at.rtr.rmbt.service.CoverageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CoverageController.class)
class CoverageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CoverageService coverageService;

    @Test
    void getCoverage_GET_expectedCoverageDTO() throws Exception {
        var response = TestObjects.coveragesDto();
        when(coverageService.getCoverage(TestConstants.DEFAULT_LATITUDE, TestConstants.DEFAULT_LONGITUDE)).thenReturn(response);
        mockMvc.perform(MockMvcRequestBuilders.get(URIConstants.COVERAGE)
                        .param("lat", TestConstants.DEFAULT_LATITUDE.toString())
                        .param("long", TestConstants.DEFAULT_LONGITUDE.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duration_ms").value(TestConstants.DEFAULT_COVERAGE_DURATION));

        verify(coverageService).getCoverage(TestConstants.DEFAULT_LATITUDE, TestConstants.DEFAULT_LONGITUDE);
    }

    @Test
    void getCoverage_latitudeMoreThan90_expectInvalidParameter() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URIConstants.COVERAGE)
                        .param("lat", "1000")
                        .param("long", TestConstants.DEFAULT_LONGITUDE.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("invalid parameters"));
    }

    @Test
    void getCoverage_POST_expectedCoverageDTO() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(URIConstants.COVERAGE)
                        .param("lat", TestConstants.DEFAULT_LATITUDE.toString())
                        .param("long", TestConstants.DEFAULT_LONGITUDE.toString()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(coverageService).getCoverage(TestConstants.DEFAULT_LATITUDE, TestConstants.DEFAULT_LONGITUDE);
    }
}