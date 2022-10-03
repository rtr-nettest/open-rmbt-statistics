package at.rtr.rmbt.response.coverage;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class CoveragesDTO {

    @JsonProperty(value = "coverages")
    private final List<CoverageDTO> coverages;

    @JsonProperty(value = "duration_ms")
    private final long durationMs;
}
