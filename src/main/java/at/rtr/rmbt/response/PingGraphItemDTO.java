package at.rtr.rmbt.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class PingGraphItemDTO {

    @JsonProperty("ping_ms")
    private double pingMs;

    @JsonProperty("time_elapsed")
    private long timeElapsed;
}
