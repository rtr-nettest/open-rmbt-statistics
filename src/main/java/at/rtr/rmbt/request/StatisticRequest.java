package at.rtr.rmbt.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Builder
@Getter
@ToString
public class StatisticRequest {

    @NotNull(message = "capabilitiesRequest must be not null")
    @Valid
    @JsonProperty(value = "capabilities")
    private final CapabilitiesRequest capabilitiesRequest;

    @JsonProperty(value = "language")
    private final String language;

    @JsonProperty(value = "quantile")
    private final Double quantile;

    @JsonProperty(value = "months")
    private final Integer months;

    @JsonProperty(value = "duration")
    private final Integer duration;

    @JsonProperty(value = "max_devices")
    private final Integer maxDevices;

    @JsonProperty(value = "type")
    private final String type;

    @JsonProperty(value = "network_type_group")
    private final String networkTypeGroup;

    @JsonProperty(value = "location_accuracy")
    private final Double locationAccuracy;

    @JsonProperty(value = "country")
    private final String country;

    @JsonProperty(value = "user_server_selection")
    private final boolean userServerSelection;

    @JsonProperty(value = "end_date")
    private final String endDate;

    @JsonProperty(value = "province")
    private final Integer province;
}
