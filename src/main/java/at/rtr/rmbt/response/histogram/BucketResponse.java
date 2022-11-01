package at.rtr.rmbt.response.histogram;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class BucketResponse {

    @JsonProperty(value = "lower_bound")
    public Double lowerBound;
    @JsonProperty(value = "upper_bound")
    public Double upperBound;
    @JsonProperty(value = "results")
    public long results = 0;
}
