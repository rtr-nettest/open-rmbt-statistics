package at.rtr.rmbt.response.adminUsage;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminUsageJsonResponse {

    @JsonProperty(value = "platforms")
    private final SumsAndValuesResponse platforms;

    @JsonProperty(value = "platforms_loopmode")
    private final SumsAndValuesResponse platformsLoopMode;

    @JsonProperty(value = "usage")
    private final SumsAndValuesResponse usage;

    @JsonProperty(value = "versions_ios")
    private final SumsAndValuesResponse versionsIos;

    @JsonProperty(value = "versions_android")
    private final SumsAndValuesResponse versionsAndroid;

    @JsonProperty(value = "versions_applet")
    private final SumsAndValuesResponse versionsApplet;

    @JsonProperty(value = "network_group_names")
    private final SumsAndValuesResponse networkGroupNames;

    @JsonProperty(value = "network_group_types")
    private final SumsAndValuesResponse networkGroupTypes;

    @JsonProperty(value = "platforms_qos")
    private final SumsAndValuesResponse platformsQos;
}
