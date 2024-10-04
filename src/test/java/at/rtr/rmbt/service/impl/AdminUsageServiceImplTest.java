package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.TestObjects;
import at.rtr.rmbt.repository.AdminUsageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class AdminUsageServiceImplTest {

    public static final Set<String> ALL_STATISTIC_PARAMETERS = Set.of("platforms", "platforms_loopmode",
            "usage", "versions_ios", "versions_android", "versions_applet",
            "network_group_names", "network_group_types", "platforms_qos");
    @Mock
    private AdminUsageRepository adminUsageRepository;
    @InjectMocks
    private AdminUsageServiceImpl adminUsageService;

    @Test
    void getAdminUsageJson_correctInvocation_expectedAdminUsageJsonResponse() {
        Calendar calendarBegin = new GregorianCalendar(TestConstants.DEFAULT_YEAR, TestConstants.DEFAULT_MONTH, 1);
        Calendar calendarEnd = new GregorianCalendar(TestConstants.DEFAULT_YEAR, TestConstants.DEFAULT_MONTH, calendarBegin.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
        Timestamp begin = new Timestamp(calendarBegin.getTimeInMillis());
        Timestamp end = new Timestamp(calendarEnd.getTimeInMillis());
        when(adminUsageRepository.getPlatforms(begin, end)).thenReturn(TestObjects.sumsAndValuesResponsePlatformsResponse());
        when(adminUsageRepository.getLoopModePlatforms(begin, end)).thenReturn(TestObjects.sumsAndValuesResponsePlatformsLoopModeResponse());
        when(adminUsageRepository.getClassicUsage(begin, end)).thenReturn(TestObjects.sumsAndValuesResponseUsageResponse());
        when(adminUsageRepository.getVersions("iOS", begin, end)).thenReturn(TestObjects.sumsAndValuesResponseVersionsIosResponse());
        when(adminUsageRepository.getVersions("Android", begin, end)).thenReturn(TestObjects.sumsAndValuesResponseVersionsAndroidResponse());
        when(adminUsageRepository.getVersions("Applet", begin, end)).thenReturn(TestObjects.sumsAndValuesResponseVersionsAppletResponse());
        when(adminUsageRepository.getNetworkGroupName(begin, end)).thenReturn(TestObjects.sumsAndValuesResponseNetworkGroupNamesResponse());
        when(adminUsageRepository.getNetworkGroupType(begin, end)).thenReturn(TestObjects.sumsAndValuesResponseNetworkGroupTypesResponse());
        when(adminUsageRepository.getQoSUsage(begin, end)).thenReturn(TestObjects.sumsAndValuesResponsePlatformsQosResponse());

        var expectedResult = TestObjects.adminUsageJsonResponse();

        var result = adminUsageService.getAdminUsageJson(TestConstants.DEFAULT_MONTH,
                TestConstants.DEFAULT_YEAR,
                ALL_STATISTIC_PARAMETERS);

        assertEquals(expectedResult, result);
    }
}