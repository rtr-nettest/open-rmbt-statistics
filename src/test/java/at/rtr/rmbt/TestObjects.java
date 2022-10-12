package at.rtr.rmbt;

import at.rtr.rmbt.response.ChoicesResponse;
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
}
