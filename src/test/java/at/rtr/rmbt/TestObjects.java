package at.rtr.rmbt;

import at.rtr.rmbt.response.ChoicesResponse;
import at.rtr.rmbt.response.HourlyStatisticResponse;
import at.rtr.rmbt.response.adminUsage.*;
import at.rtr.rmbt.response.coverage.CoverageDTO;
import at.rtr.rmbt.response.coverage.CoveragesDTO;
import at.rtr.rmbt.utils.QueryParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.GeoJsonObject;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TestObjects {

    static CoveragesDTO coveragesDto() {
        var coverages = List.of(TestObjects.coverageDto());
        return new CoveragesDTO(coverages, TestConstants.DEFAULT_COVERAGE_DURATION);
    }

    static CoverageDTO coverageDto() {
        return new CoverageDTO(
                TestConstants.DEFAULT_OPERATOR,
                TestConstants.DEFAULT_RASTER,
                TestConstants.DEFAULT_DOWNLOAD_KBIT_MAX,
                TestConstants.DEFAULT_UPLOAD_KBIT_MAX,
                TestConstants.DEFAULT_DOWNLOAD_KBIT_NORMAL,
                TestConstants.DEFAULT_UPLOAD_KBIT_NORMAL,
                TestConstants.DEFAULT_TECHNOLOGY,
                TestConstants.DEFAULT_LAST_UPDATED,
                TestObjects.rasterGeoJson()

        );
    }

    static GeoJsonObject rasterGeoJson() {
        try {
            return new ObjectMapper().readValue(TestConstants.DEFAULT_GEO_JSON_OBJECT, GeoJsonObject.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    static ChoicesResponse choicesResponse() {
        return new ChoicesResponse(Set.of(TestConstants.DEFAULT_COUNTRY_MOBILE),
                Set.of(TestConstants.DEFAULT_PROVIDER_MOBILE),
                Set.of(TestConstants.DEFAULT_PROVIDER));
    }

    static QueryParser queryParser() {
        QueryParser queryParser = new QueryParser();
        queryParser.parseQuery(TestObjects.parametersMap());
        return queryParser;
    }

    static MultiValueMap<String, String> parametersMap() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.put("country_location", List.of(TestConstants.DEFAULT_COUNTRY_LOCATION));
        map.put("download_kbit", List.of(TestConstants.DEFAULT_DOWNLOAD_KBIT.toString()));
        return map;
    }

    static Map<String, Long> statisticsResponse() {
        return Map.of("5min", TestConstants.DEFAULT_STATISTICS_5MIN,
                "30min", TestConstants.DEFAULT_STATISTICS_30MIN,
                "60min", TestConstants.DEFAULT_STATISTICS_60MIN,
                "12h", TestConstants.DEFAULT_STATISTICS_12H,
                "24h", TestConstants.DEFAULT_STATISTICS_24H,
                "7d", TestConstants.DEFAULT_STATISTICS_7D);
    }

    static AdminUsageJsonResponse adminUsageJsonResponse() {
        return new AdminUsageJsonResponse(
                TestObjects.sumsAndValuesResponsePlatformsResponse(),
                TestObjects.sumsAndValuesResponsePlatformsLoopModeResponse(),
                TestObjects.sumsAndValuesResponseUsageResponse(),
                TestObjects.sumsAndValuesResponseVersionsIosResponse(),
                TestObjects.sumsAndValuesResponseVersionsAndroidResponse(),
                TestObjects.sumsAndValuesResponseVersionsAppletResponse(),
                TestObjects.sumsAndValuesResponseNetworkGroupNamesResponse(),
                TestObjects.sumsAndValuesResponseNetworkGroupTypesResponse(),
                TestObjects.sumsAndValuesResponsePlatformsQosResponse()
        );
    }

    static SumsAndValuesResponse sumsAndValuesResponsePlatformsQosResponse() {
        return TestObjects.sumsAndValuesResponse(TestConstants.DEFAULT_FIELD_PLATFORMS_QOS,
                TestConstants.DEFAULT_SUM_PLATFORMS_QOS,
                TestConstants.DEFAULT_DAY_PLATFORMS_QOS);
    }


    static SumsAndValuesResponse sumsAndValuesResponseNetworkGroupTypesResponse() {
        return TestObjects.sumsAndValuesResponse(TestConstants.DEFAULT_FIELD_NETWORK_GROUP_TYPES,
                TestConstants.DEFAULT_SUM_NETWORK_GROUP_TYPES,
                TestConstants.DEFAULT_DAY_NETWORK_GROUP_TYPES);
    }

    static SumsAndValuesResponse sumsAndValuesResponseNetworkGroupNamesResponse() {
        return TestObjects.sumsAndValuesResponse(TestConstants.DEFAULT_FIELD_NETWORK_GROUP_NAMES,
                TestConstants.DEFAULT_SUM_NETWORK_GROUP_NAMES,
                TestConstants.DEFAULT_DAY_NETWORK_GROUP_NAMES);
    }

    static SumsAndValuesResponse sumsAndValuesResponseVersionsAppletResponse() {
        return TestObjects.sumsAndValuesResponse(TestConstants.DEFAULT_FIELD_VERSIONS_APPLET,
                TestConstants.DEFAULT_SUM_VERSIONS_APPLET,
                TestConstants.DEFAULT_DAY_VERSIONS_APPLET);
    }

    static SumsAndValuesResponse sumsAndValuesResponseVersionsAndroidResponse() {
        return TestObjects.sumsAndValuesResponse(TestConstants.DEFAULT_FIELD_VERSIONS_ANDROID,
                TestConstants.DEFAULT_SUM_VERSIONS_ANDROID,
                TestConstants.DEFAULT_DAY_VERSIONS_ANDROID);
    }

    static SumsAndValuesResponse sumsAndValuesResponseVersionsIosResponse() {
        return TestObjects.sumsAndValuesResponse(TestConstants.DEFAULT_FIELD_VERSIONS_IOS,
                TestConstants.DEFAULT_SUM_VERSIONS_IOS,
                TestConstants.DEFAULT_DAY_VERSIONS_IOS);
    }

    static SumsAndValuesResponse sumsAndValuesResponseUsageResponse() {
        return new SumsAndValuesResponse(
                List.of(new SumsResponse("aborted", TestConstants.DEFAULT_SUM_USAGE_ABORTED),
                        new SumsResponse("clients", TestConstants.DEFAULT_SUM_USAGE_CLIENTS),
                        new SumsResponse("finished", TestConstants.DEFAULT_SUM_USAGE_FINISHED),
                        new SumsResponse("ips", TestConstants.DEFAULT_SUM_USAGE_IPS),
                        new SumsResponse("tests", TestConstants.DEFAULT_SUM_USAGE_TESTS)),
                List.of(TestObjects.valuesResponseUsage(TestConstants.DEFAULT_DAY_USAGE)));
    }

    static SumsAndValuesResponse sumsAndValuesResponsePlatformsResponse() {
        return TestObjects.sumsAndValuesResponse(TestConstants.DEFAULT_FIELD_PLATFORM,
                TestConstants.DEFAULT_SUM_PLATFORM,
                TestConstants.DEFAULT_DAY_PLATFORM);
    }

    static SumsAndValuesResponse sumsAndValuesResponsePlatformsLoopModeResponse() {
        return TestObjects.sumsAndValuesResponse(TestConstants.DEFAULT_FIELD_PLATFORM_LOOP_MODE,
                TestConstants.DEFAULT_SUM_PLATFORM_LOOP_MODE,
                TestConstants.DEFAULT_DAY_PLATFORM_LOOP_MODE);
    }

    static SumsAndValuesResponse sumsAndValuesResponse(String field, Long sum, Long day) {
        return new SumsAndValuesResponse(
                List.of(TestObjects.sumsResponse(field, sum)),
                List.of(TestObjects.valuesResponse(day, field, sum)));
    }

    static ValuesResponse valuesResponse(Long day, String field, Long value) {
        return new ValuesResponse(day,
                List.of(TestObjects.valueResponse(field, value)));
    }

    static ValuesResponse valuesResponseUsage(Long day) {
        return new ValuesResponse(day,
                List.of(TestObjects.valueResponse("aborted", TestConstants.DEFAULT_SUM_USAGE_ABORTED),
                        TestObjects.valueResponse("clients", TestConstants.DEFAULT_SUM_USAGE_CLIENTS),
                        TestObjects.valueResponse("finished", TestConstants.DEFAULT_SUM_USAGE_FINISHED),
                        TestObjects.valueResponse("ips", TestConstants.DEFAULT_SUM_USAGE_IPS),
                        TestObjects.valueResponse("tests", TestConstants.DEFAULT_SUM_USAGE_TESTS)));
    }

    static ValueResponse valueResponse(String field, Long value) {
        return new ValueResponse(field, value);
    }

    static SumsResponse sumsResponse(String value, Long sum) {
        return new SumsResponse(value, sum);
    }

    static HourlyStatisticResponse hourlyStatisticResponse() {
        return new HourlyStatisticResponse(TestConstants.DEFAULT_QUANTILE_DOWN,
                TestConstants.DEFAULT_QUANTILE_UP,
                TestConstants.DEFAULT_QUANTILE_PING,
                TestConstants.DEFAULT_HOUR,
                TestConstants.DEFAULT_COUNT);
    }
}
