package at.rtr.rmbt.response.adminUsage;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ValuesResponse {

    @JsonProperty(value = "day")
    private final Long day;

    @JsonProperty(value = "values")
    private final List<ValueResponse> valueResponses;
}
