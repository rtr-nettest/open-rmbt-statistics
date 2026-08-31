package at.rtr.rmbt.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.geojson.Geometry;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class FencesItemDTO {

    // based on RMBTControlServer, src/main/java/at/rtr/rmbt/model/Fences.java

    @Schema(description = "Number of fence, starts with 1", example = "1")
    @JsonProperty("fence_id") // int4
    private final Long fenceId;

    @Schema(description = "Numeric id of technology", example = "41")
    @JsonProperty("technology_id") // int4
    private final Long technologyId;

    @Schema(description = "Median ping value in ms, can be null", example = "4.42")
    @JsonProperty("avg_ping_ms") // float8
    private final Double avgPingMs;

    @Schema(description = "Name of technology", example = "5G SA")
    @JsonProperty("technology") // varchar
    private final String technology;

    @Schema(description = "Time offset relative to /coverageRequest in ms, can be negative", example = "13506")
    @JsonProperty("offset_ms") // int4
    private final Long offsetMs;

    @Schema(description = "Duration of client within fence in ms", example = "2123")
    @JsonProperty("duration_ms") // int4
    private final Long durationMs;

    @Schema(description = "Radius of fence in meter", example = "25.4")
    @JsonProperty("radius") // int4
    private final Double radius;

    @Schema(description = "Longitude of center of fence", example = "16.349006434053081")
    @JsonProperty("longitude") // int4
    private final Double longitude;

    @Schema(description = "Latitude of center of fence", example = "48.197872928901063")
    @JsonProperty("latitude") // int4
    private final Double latitude;

    @Schema(description = "Timestamp of fence, Unixtime in ms", example = "1768141769894")
    @JsonProperty("fence_time") // timestamptz (converted)
    private final Long fenceTime;

    @Schema(description = "Minimum signal (RSRP) of fence in dBm", example = "-103.5")
    @JsonProperty(value = "signal")
    private final Double signal;

    @Schema(description = "Location accuracy of the fence center in m, as reported by the client", example = "13.014")
    @JsonProperty("accuracy") // float8
    private final Double accuracy;

    @Schema(description = "Location provider of the fence center, as reported by the client", example = "gps")
    @JsonProperty("provider") // varchar
    private final String provider;

    @Schema(description = "Altitude of the fence in meter, as reported by the client", example = "2183.4")
    @JsonProperty("altitude") // float8
    private final Double altitude;

    @Schema(description = "Heading of the fence in degrees from north, as reported by the client", example = "271.5")
    @JsonProperty("bearing") // float8
    private final Double bearing;

    @Schema(description = "Speed of the fence in meter per second, as reported by the client", example = "13.8")
    @JsonProperty("speed") // float8
    private final Double speed;

}
