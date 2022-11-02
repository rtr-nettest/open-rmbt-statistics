package at.rtr.rmbt.response.histogram;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@ToString
@EqualsAndHashCode
public class BucketResponse implements Serializable {

    @JsonProperty(value = "lower_bound")
    public Double lowerBound;
    @JsonProperty(value = "upper_bound")
    public Double upperBound;
    @JsonProperty(value = "results")
    public long results = 0;
}
