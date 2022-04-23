package at.rtr.rmbt.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;


@Builder
@Getter
@ToString
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Statistic request")
public class StatisticRequest {

    @JsonProperty(value = "capabilities")
    private final CapabilitiesRequest capabilitiesRequest;

    @JsonProperty(value = "language")
    @Schema(type = "string", example = "en", description = "The language that should be exported.")
    private final String language;

    @JsonProperty(value = "quantile")
    @Schema(description = "Quantile", type = "number", example = "0.5")
    private final Double quantile;

    @JsonProperty(value = "months")
    @Schema(description = "The month that should be calculated.", type = "integer", example = "1")
    private final Integer months;

    @JsonProperty(value = "duration")
    @Schema(description = "The duration that should be calculated.", type = "integer", example = "1")
    private final Integer duration;

    @JsonProperty(value = "max_devices")
    @Schema(description = "The number of device that should be calculated.", type = "integer", example = "1")
    private final Integer maxDevices;

    @JsonProperty(value = "type")
    @Schema(description = "The type of measurement", type = "string", example = "mobile")
    private final String type;

    @JsonProperty(value = "network_type_group")
    @Schema(description = "Type of the network, e.g. MOBILE, LAN, WLAN.", type = "string")
    private final String networkTypeGroup;

    @JsonProperty(value = "location_accuracy")
    @Schema(description = "Estimation of accuracy of client location in meters", type = "string")
    private final Double locationAccuracy;

    @JsonProperty(value = "country")
    @Schema(description = "The country of measurement in ISO 3166.", type = "string", example = "en")
    private final String country;

    @JsonProperty(value = "user_server_selection")
    @Schema(description = "Legacy", type = "boolean")
    private final boolean userServerSelection;

    @JsonProperty(value = "end_date")
    @Schema(description = "UTC date and time when calculation was ended yyyy-MM-dd HH:mm:ss", type = "string")
    private final String endDate;

    @JsonProperty(value = "province")
    @Schema(description = "Code of province gkz bev", type = "integer")
    private final Integer province;

    @JsonProperty(value = "timezone")
    @Schema(description = "Timezone", type = "string", example = "Europe/Vienna")
    private final String timezone;
}
