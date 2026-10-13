package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.constant.AdminSettingConfig;
import at.rtr.rmbt.constant.Constants;
import at.rtr.rmbt.model.Settings;
import at.rtr.rmbt.repository.SettingsRepository;
import at.rtr.rmbt.service.SettingsCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeoAccuracyLimitServiceImplTest {

    @Mock
    private SettingsRepository settingsRepository;

    private SettingsCache settingsCache;
    private GeoAccuracyLimitServiceImpl geoAccuracyLimitService;

    @BeforeEach
    void setUp() {
        settingsCache = new SettingsCache(settingsRepository);
        geoAccuracyLimitService = new GeoAccuracyLimitServiceImpl(settingsCache);
    }

    private void mockSetting(String value) {
        when(settingsRepository.findAll()).thenReturn(value == null ? List.of() : List.of(Settings.builder()
                .id(1L).key(AdminSettingConfig.GEO_ACCURACY_DETAIL_LIMIT_KEY).value(value).build()));
        settingsCache.refresh();
    }

    @Test
    void validValue_used() {
        mockSetting("2000");
        assertEquals(2000d, geoAccuracyLimitService.getGeoAccuracyDetailLimit());
    }

    @Test
    void missingRow_default() {
        mockSetting(null);
        assertEquals(Constants.RMBT_GEO_ACCURACY_DETAIL_LIMIT, geoAccuracyLimitService.getGeoAccuracyDetailLimit());
    }

    @Test
    void invalidValues_default() {
        for (String invalid : new String[]{"abc", "", "-5", "0", "NaN", "Infinity"}) {
            mockSetting(invalid);
            assertEquals(Constants.RMBT_GEO_ACCURACY_DETAIL_LIMIT, geoAccuracyLimitService.getGeoAccuracyDetailLimit(), invalid);
        }
    }

    @Test
    void applyGeoAccuracyDetailLimit_replacesAllPlaceholders() {
        mockSetting("2000");
        String sql = "a < " + Constants.GEO_ACCURACY_DETAIL_LIMIT_PLACEHOLDER + " AND b < " + Constants.GEO_ACCURACY_DETAIL_LIMIT_PLACEHOLDER;
        assertEquals("a < 2000.0 AND b < 2000.0", geoAccuracyLimitService.applyGeoAccuracyDetailLimit(sql));
    }
}
