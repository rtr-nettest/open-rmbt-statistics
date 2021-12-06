package at.rtr.rmbt.controller;


import at.rtr.rmbt.service.ApplicationVersionService;
import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.TestUtils;
import at.rtr.rmbt.constant.URIConstants;
import at.rtr.rmbt.response.ApplicationVersionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ExtendWith(SpringExtension.class)
class ApplicationVersionControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private ApplicationVersionService applicationVersionService;

    @BeforeEach
    void setUp() throws Exception {
        ApplicationVersionController applicationVersionController = new ApplicationVersionController(applicationVersionService);
        mockMvc = MockMvcBuilders.standaloneSetup(applicationVersionController)
                .build();
    }

    @Test
    void getApplicationVersion_whenCommonData_expectApplicationVersionResponse() throws Exception {
        var applicationVersionResponse = getApplicationVersionResponse();
        when(applicationVersionService.getApplicationVersion()).thenReturn(applicationVersionResponse);
        mockMvc.perform(MockMvcRequestBuilders.get(URIConstants.VERSION))
                .andDo(print())
                .andExpect(content().json(TestUtils.asJsonString(applicationVersionResponse)));
    }

    private ApplicationVersionResponse getApplicationVersionResponse() {
        return ApplicationVersionResponse.builder()
                .systemUUID(TestConstants.DEFAULT_SYSTEM_UUID_VALUE)
                .version(TestConstants.DEFAULT_CONTROL_SERVER_VERSION)
                .host(TestConstants.DEFAULT_APPLICATION_HOST)
                .build();
    }
}
