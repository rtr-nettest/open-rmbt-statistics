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
public class SumsAndValuesResponse {

    @JsonProperty(value = "sums")
    private final List<SumsResponse> sums;

    @JsonProperty(value = "values")
    private final List<ValuesResponse> values;
}
