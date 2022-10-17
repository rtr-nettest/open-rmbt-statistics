package at.rtr.rmbt.response.adminUsage;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;


@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ValueResponse {

    @JsonProperty(value = "field")
    private final String field;

    @JsonProperty(value = "value")
    private final Long value;
}
