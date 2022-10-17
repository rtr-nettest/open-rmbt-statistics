package at.rtr.rmbt.controller;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.TestObjects;
import at.rtr.rmbt.constant.URIConstants;
import at.rtr.rmbt.service.AdminUsageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUsageController.class)
class AdminUsageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminUsageService adminUsageService;

    @Test
    void getAdminUsageJson_correctInvocation_expectedAdminUsageJsonResponse() throws Exception {
        var response = TestObjects.adminUsageJsonResponse();
        when(adminUsageService.getAdminUsageJson(TestConstants.DEFAULT_MONTH, TestConstants.DEFAULT_YEAR, Set.of(TestConstants.DEFAULT_FIELD))).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get(URIConstants.ADMIN_USAGE_JSON)
                        .param("month", String.valueOf(TestConstants.DEFAULT_MONTH))
                        .param("year", String.valueOf(TestConstants.DEFAULT_YEAR))
                        .param("statistic", TestConstants.DEFAULT_FIELD))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platforms.sums[0].field").value(TestConstants.DEFAULT_FIELD_PLATFORM))
                .andExpect(jsonPath("$.platforms.sums[0].sum").value(TestConstants.DEFAULT_SUM_PLATFORM))
                .andExpect(jsonPath("$.platforms.values[0].day").value(TestConstants.DEFAULT_DAY_PLATFORM))
                .andExpect(jsonPath("$.platforms.values[0].values[0].field").value(TestConstants.DEFAULT_FIELD_PLATFORM))
                .andExpect(jsonPath("$.platforms.values[0].values[0].value").value(TestConstants.DEFAULT_SUM_PLATFORM))
                .andExpect(jsonPath("$.platforms_loopmode.sums[0].field").value(TestConstants.DEFAULT_FIELD_PLATFORM_LOOP_MODE))
                .andExpect(jsonPath("$.platforms_loopmode.sums[0].sum").value(TestConstants.DEFAULT_SUM_PLATFORM_LOOP_MODE))
                .andExpect(jsonPath("$.platforms_loopmode.values[0].day").value(TestConstants.DEFAULT_DAY_PLATFORM_LOOP_MODE))
                .andExpect(jsonPath("$.platforms_loopmode.values[0].values[0].field").value(TestConstants.DEFAULT_FIELD_PLATFORM_LOOP_MODE))
                .andExpect(jsonPath("$.platforms_loopmode.values[0].values[0].value").value(TestConstants.DEFAULT_SUM_PLATFORM_LOOP_MODE))
                .andExpect(jsonPath("$.usage.sums[0].field").value("aborted"))
                .andExpect(jsonPath("$.usage.sums[0].sum").value(TestConstants.DEFAULT_SUM_USAGE_ABORTED))
                .andExpect(jsonPath("$.usage.sums[1].field").value("clients"))
                .andExpect(jsonPath("$.usage.sums[1].sum").value(TestConstants.DEFAULT_SUM_USAGE_CLIENTS))
                .andExpect(jsonPath("$.usage.sums[2].field").value("finished"))
                .andExpect(jsonPath("$.usage.sums[2].sum").value(TestConstants.DEFAULT_SUM_USAGE_FINISHED))
                .andExpect(jsonPath("$.usage.sums[3].field").value("ips"))
                .andExpect(jsonPath("$.usage.sums[3].sum").value(TestConstants.DEFAULT_SUM_USAGE_IPS))
                .andExpect(jsonPath("$.usage.sums[4].field").value("tests"))
                .andExpect(jsonPath("$.usage.sums[4].sum").value(TestConstants.DEFAULT_SUM_USAGE_TESTS))
                .andExpect(jsonPath("$.usage.values[0].day").value(TestConstants.DEFAULT_DAY_USAGE))
                .andExpect(jsonPath("$.usage.values[0].values[0].field").value("aborted"))
                .andExpect(jsonPath("$.usage.values[0].values[0].value").value(TestConstants.DEFAULT_SUM_USAGE_ABORTED))
                .andExpect(jsonPath("$.usage.values[0].values[1].field").value("clients"))
                .andExpect(jsonPath("$.usage.values[0].values[1].value").value(TestConstants.DEFAULT_SUM_USAGE_CLIENTS))
                .andExpect(jsonPath("$.usage.values[0].values[2].field").value("finished"))
                .andExpect(jsonPath("$.usage.values[0].values[2].value").value(TestConstants.DEFAULT_SUM_USAGE_FINISHED))
                .andExpect(jsonPath("$.usage.values[0].values[3].field").value("ips"))
                .andExpect(jsonPath("$.usage.values[0].values[3].value").value(TestConstants.DEFAULT_SUM_USAGE_IPS))
                .andExpect(jsonPath("$.usage.values[0].values[4].field").value("tests"))
                .andExpect(jsonPath("$.usage.values[0].values[4].value").value(TestConstants.DEFAULT_SUM_USAGE_TESTS))
                .andExpect(jsonPath("$.versions_ios.sums[0].field").value(TestConstants.DEFAULT_FIELD_VERSIONS_IOS))
                .andExpect(jsonPath("$.versions_ios.sums[0].sum").value(TestConstants.DEFAULT_SUM_VERSIONS_IOS))
                .andExpect(jsonPath("$.versions_ios.values[0].day").value(TestConstants.DEFAULT_DAY_VERSIONS_IOS))
                .andExpect(jsonPath("$.versions_ios.values[0].values[0].field").value(TestConstants.DEFAULT_FIELD_VERSIONS_IOS))
                .andExpect(jsonPath("$.versions_ios.values[0].values[0].value").value(TestConstants.DEFAULT_SUM_VERSIONS_IOS))
                .andExpect(jsonPath("$.versions_android.sums[0].field").value(TestConstants.DEFAULT_FIELD_VERSIONS_ANDROID))
                .andExpect(jsonPath("$.versions_android.sums[0].sum").value(TestConstants.DEFAULT_SUM_VERSIONS_ANDROID))
                .andExpect(jsonPath("$.versions_android.values[0].day").value(TestConstants.DEFAULT_DAY_VERSIONS_ANDROID))
                .andExpect(jsonPath("$.versions_android.values[0].values[0].field").value(TestConstants.DEFAULT_FIELD_VERSIONS_ANDROID))
                .andExpect(jsonPath("$.versions_android.values[0].values[0].value").value(TestConstants.DEFAULT_SUM_VERSIONS_ANDROID))
                .andExpect(jsonPath("$.versions_applet.sums[0].field").value(TestConstants.DEFAULT_FIELD_VERSIONS_APPLET))
                .andExpect(jsonPath("$.versions_applet.sums[0].sum").value(TestConstants.DEFAULT_SUM_VERSIONS_APPLET))
                .andExpect(jsonPath("$.versions_applet.values[0].day").value(TestConstants.DEFAULT_DAY_VERSIONS_APPLET))
                .andExpect(jsonPath("$.versions_applet.values[0].values[0].field").value(TestConstants.DEFAULT_FIELD_VERSIONS_APPLET))
                .andExpect(jsonPath("$.versions_applet.values[0].values[0].value").value(TestConstants.DEFAULT_SUM_VERSIONS_APPLET))
                .andExpect(jsonPath("$.network_group_names.sums[0].field").value(TestConstants.DEFAULT_FIELD_NETWORK_GROUP_NAMES))
                .andExpect(jsonPath("$.network_group_names.sums[0].sum").value(TestConstants.DEFAULT_SUM_NETWORK_GROUP_NAMES))
                .andExpect(jsonPath("$.network_group_names.values[0].day").value(TestConstants.DEFAULT_DAY_NETWORK_GROUP_NAMES))
                .andExpect(jsonPath("$.network_group_names.values[0].values[0].field").value(TestConstants.DEFAULT_FIELD_NETWORK_GROUP_NAMES))
                .andExpect(jsonPath("$.network_group_names.values[0].values[0].value").value(TestConstants.DEFAULT_SUM_NETWORK_GROUP_NAMES))
                .andExpect(jsonPath("$.network_group_types.sums[0].field").value(TestConstants.DEFAULT_FIELD_NETWORK_GROUP_TYPES))
                .andExpect(jsonPath("$.network_group_types.sums[0].sum").value(TestConstants.DEFAULT_SUM_NETWORK_GROUP_TYPES))
                .andExpect(jsonPath("$.network_group_types.values[0].day").value(TestConstants.DEFAULT_DAY_NETWORK_GROUP_TYPES))
                .andExpect(jsonPath("$.network_group_types.values[0].values[0].field").value(TestConstants.DEFAULT_FIELD_NETWORK_GROUP_TYPES))
                .andExpect(jsonPath("$.network_group_types.values[0].values[0].value").value(TestConstants.DEFAULT_SUM_NETWORK_GROUP_TYPES))
                .andExpect(jsonPath("$.platforms_qos.sums[0].field").value(TestConstants.DEFAULT_FIELD_PLATFORMS_QOS))
                .andExpect(jsonPath("$.platforms_qos.sums[0].sum").value(TestConstants.DEFAULT_SUM_PLATFORMS_QOS))
                .andExpect(jsonPath("$.platforms_qos.values[0].day").value(TestConstants.DEFAULT_DAY_PLATFORMS_QOS))
                .andExpect(jsonPath("$.platforms_qos.values[0].values[0].field").value(TestConstants.DEFAULT_FIELD_PLATFORMS_QOS))
                .andExpect(jsonPath("$.platforms_qos.values[0].values[0].value").value(TestConstants.DEFAULT_SUM_PLATFORMS_QOS));
    }
}