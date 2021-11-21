package com.example.statisticserver.service;

import com.example.statisticserver.TestConstants;
import com.example.statisticserver.constant.AdminSettingConfig;
import com.example.statisticserver.model.Settings;
import com.example.statisticserver.repository.SettingsRepository;
import com.example.statisticserver.service.impl.ApplicationVersionServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ApplicationVersionServiceTest {

    private ApplicationVersionService applicationVersionService;

    @MockBean
    private SettingsRepository settingsRepository;

    @MockBean
    private Settings settings;

    @BeforeEach
    public void setUp() {
        applicationVersionService = new ApplicationVersionServiceImpl(settingsRepository);
        ReflectionTestUtils.setField(applicationVersionService, "branch", TestConstants.DEFAULT_GIT_BRANCH);
        ReflectionTestUtils.setField(applicationVersionService, "describe", TestConstants.DEFAULT_GIT_COMMIT_ID_DESCRIBE);
        ReflectionTestUtils.setField(applicationVersionService, "applicationHost", TestConstants.DEFAULT_APPLICATION_HOST);
    }

    @Test
    public void getApplicationVersion_whenCommonData_expectApplicationVersionResponse() {
        when(settingsRepository.findFirstByKeyAndLangIsNullOrKeyAndLangOrderByLang(AdminSettingConfig.SYSTEM_UUID_KEY, AdminSettingConfig.SYSTEM_UUID_KEY, StringUtils.EMPTY)).thenReturn(Optional.of(settings));
        when(settings.getValue()).thenReturn(TestConstants.DEFAULT_SYSTEM_UUID_VALUE);

        var response = applicationVersionService.getApplicationVersion();

        assertEquals(TestConstants.DEFAULT_APPLICATION_HOST, response.getHost());
        assertEquals(TestConstants.DEFAULT_CONTROL_SERVER_VERSION, response.getVersion());
        assertEquals(TestConstants.DEFAULT_SYSTEM_UUID_VALUE, response.getSystemUUID());
    }
}