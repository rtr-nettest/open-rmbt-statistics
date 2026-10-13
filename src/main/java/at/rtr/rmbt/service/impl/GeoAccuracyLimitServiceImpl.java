package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.constant.AdminSettingConfig;
import at.rtr.rmbt.constant.Constants;
import at.rtr.rmbt.service.GeoAccuracyLimitService;
import at.rtr.rmbt.service.SettingsCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeoAccuracyLimitServiceImpl implements GeoAccuracyLimitService {

    private final SettingsCache settingsCache;

    /**
     * Reads the setting from the {@link SettingsCache} (no database query); a missing, invalid or
     * non-positive value yields the hard-coded default.
     */
    @Override
    public double getGeoAccuracyDetailLimit() {
        final double limit = settingsCache.getDouble(AdminSettingConfig.GEO_ACCURACY_DETAIL_LIMIT_KEY,
                Constants.RMBT_GEO_ACCURACY_DETAIL_LIMIT);
        return Double.isFinite(limit) && limit > 0 ? limit : Constants.RMBT_GEO_ACCURACY_DETAIL_LIMIT;
    }

    @Override
    public String applyGeoAccuracyDetailLimit(String sql) {
        return sql.replace(Constants.GEO_ACCURACY_DETAIL_LIMIT_PLACEHOLDER, Double.toString(getGeoAccuracyDetailLimit()));
    }
}
