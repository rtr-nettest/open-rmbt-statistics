package at.rtr.rmbt.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("fence_id") // int4
    private final Long fenceId;

    @JsonProperty("technology_id") // int4
    private final Long technologyId;

    @JsonProperty("technology") // varchar
    private final String technology;

    @JsonProperty("offset_ms") // int4
    private final Long offsetMs;

    @JsonProperty("duration_ms") // int4
    private final Long durationMs;

    @JsonProperty("radius") // int4
    private final Integer radius;

    @JsonProperty("longitude") // int4
    private final Double longitude;

    @JsonProperty("latitude") // int4
    private final Double latitude;
}
