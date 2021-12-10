package at.rtr.rmbt.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
@Builder
@EqualsAndHashCode
public class ClassificationRequest {

    @ApiModelProperty(value = "Amount of classification items supported by client", example = "5")
    @JsonProperty(value = "count")
    @NotNull
    private final Integer count;

}
