package at.rtr.rmbt.service;

/**
 * Provides the {@code geo_accuracy_limit_detail} setting: the accuracy bound (meters) below which
 * a location is considered valid. The value comes from the {@link SettingsCache}, so reading it
 * never causes a database query.
 */
public interface GeoAccuracyLimitService {

    /**
     * @return the current accuracy limit in meters
     */
    double getGeoAccuracyDetailLimit();

    /**
     * Replaces {@link at.rtr.rmbt.constant.Constants#GEO_ACCURACY_DETAIL_LIMIT_PLACEHOLDER} in the
     * given SQL with the current accuracy limit.
     *
     * @param sql SQL containing the placeholder
     * @return the SQL with the current limit
     */
    String applyGeoAccuracyDetailLimit(String sql);
}
