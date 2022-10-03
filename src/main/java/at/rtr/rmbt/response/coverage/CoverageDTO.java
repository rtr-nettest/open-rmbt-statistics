package at.rtr.rmbt.response.coverage;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.geojson.GeoJsonObject;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class CoverageDTO {

    @JsonProperty(value = "operator")
    private final String operator;
    @JsonProperty(value = "raster")
    private final String raster;
    @JsonProperty(value = "download_kbit_max")
    private final Integer downloadKbitMax;
    @JsonProperty(value = "upload_kbit_max")
    private final Integer uploadKbitMax;
    @JsonProperty(value = "download_kbit_normal")
    private final Integer downloadKbitNormal;
    @JsonProperty(value = "upload_kbit_normal")
    private final Integer uploadKbitNormal;
    @JsonProperty(value = "technology")
    private final String technology;
    @JsonProperty(value = "last_updated")
    private final String lastUpdated;
    @JsonProperty(value = "raster_geo_json")
    private final GeoJsonObject rasterGeoJson;
}
