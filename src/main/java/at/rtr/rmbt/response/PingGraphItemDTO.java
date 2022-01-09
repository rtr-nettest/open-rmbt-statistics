package at.rtr.rmbt.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class PingGraphItemDTO {

    @JsonProperty("ping_ms")
    private final double pingMs;

    @JsonProperty("time_elapsed")
    private final long timeElapsed;
}
