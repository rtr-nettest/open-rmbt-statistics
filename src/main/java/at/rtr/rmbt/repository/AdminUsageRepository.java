package at.rtr.rmbt.repository;

import at.rtr.rmbt.response.adminUsage.SumsAndValuesResponse;

import java.sql.Timestamp;

public interface AdminUsageRepository {
    SumsAndValuesResponse getPlatforms(Timestamp begin, Timestamp end);

    SumsAndValuesResponse getLoopModePlatforms(Timestamp begin, Timestamp end);

    SumsAndValuesResponse getClassicUsage(Timestamp begin, Timestamp end);

    SumsAndValuesResponse getVersions(String platform, Timestamp begin, Timestamp end);

    SumsAndValuesResponse getNetworkGroupName(Timestamp begin, Timestamp end);

    SumsAndValuesResponse getNetworkGroupType(Timestamp begin, Timestamp end);

    SumsAndValuesResponse getQoSUsage(Timestamp begin, Timestamp end);
}
