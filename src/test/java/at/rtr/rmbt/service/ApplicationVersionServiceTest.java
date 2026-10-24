package at.rtr.rmbt.service;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.constant.AdminSettingConfig;
import at.rtr.rmbt.model.Settings;
import at.rtr.rmbt.repository.SettingsRepository;
import at.rtr.rmbt.service.impl.ApplicationVersionServiceImpl;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApplicationVersionServiceTest {

    private ApplicationVersionService applicationVersionService;

    @Mock
    private SettingsRepository settingsRepository;

    private SettingsCache settingsCache;

    @Mock
    private ObjectProvider<RedisConnectionFactory> redisConnectionFactory;

    @BeforeEach
    public void setUp() {
        settingsCache = new SettingsCache(settingsRepository);
        applicationVersionService = new ApplicationVersionServiceImpl(settingsCache, redisConnectionFactory);
        ReflectionTestUtils.setField(applicationVersionService, "branch", TestConstants.DEFAULT_GIT_BRANCH);
        ReflectionTestUtils.setField(applicationVersionService, "describe", TestConstants.DEFAULT_GIT_COMMIT_ID_DESCRIBE);
        ReflectionTestUtils.setField(applicationVersionService, "buildTime", TestConstants.DEFAULT_GIT_BUILD_TIME);
        ReflectionTestUtils.setField(applicationVersionService, "applicationHost", TestConstants.DEFAULT_APPLICATION_HOST);
    }

    @Test
    void getApplicationVersion_whenCommonData_expectApplicationVersionResponse() {
        when(settingsRepository.findAll()).thenReturn(List.of(Settings.builder()
                .id(1L).key(AdminSettingConfig.SYSTEM_UUID_KEY).value(TestConstants.DEFAULT_SYSTEM_UUID_VALUE).build()));
        settingsCache.refresh();

        var response = applicationVersionService.getApplicationVersion();

        assertEquals(TestConstants.DEFAULT_APPLICATION_HOST, response.getHost());
        assertEquals(TestConstants.DEFAULT_CONTROL_SERVER_VERSION, response.getVersion());
        assertEquals(TestConstants.DEFAULT_SYSTEM_UUID_VALUE, response.getSystemUUID());
        assertEquals("none", response.getCache());
    }
}