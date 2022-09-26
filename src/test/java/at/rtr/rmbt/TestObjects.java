package at.rtr.rmbt;

import at.rtr.rmbt.response.coverage.CoverageDTO;
import at.rtr.rmbt.response.coverage.CoveragesDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.GeoJsonObject;

import java.util.List;

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
}
