package at.rtr.rmbt.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class HourlyStatisticResponse implements Serializable {

    @JsonProperty(value = "quantile_down")
    private final double quantileDown;
    @JsonProperty(value = "quantile_up")
    private final double quantileUp;
    @JsonProperty(value = "quantile_ping")
    private final double quantilePing;
    @JsonProperty(value = "hour")
    private final float hourOfTheDay;
    @JsonProperty(value = "count")
    private final long count;
}
