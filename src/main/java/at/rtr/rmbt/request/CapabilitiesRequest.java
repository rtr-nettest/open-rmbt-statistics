package at.rtr.rmbt.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter
@Builder
@EqualsAndHashCode
public class CapabilitiesRequest {

    @NotNull
    @Valid
    @JsonProperty(value = "classification")
    private final ClassificationRequest classification;

    @NotNull
    @Valid
    @JsonProperty(value = "qos")
    private final QosRequest qos;

    @NotNull
    @JsonProperty(value = "RMBThttp")
    @ApiModelProperty(notes = "True, if the client can handle the RMBThttp protocol", example = "true")
    private final boolean rmbtHttp;

}
