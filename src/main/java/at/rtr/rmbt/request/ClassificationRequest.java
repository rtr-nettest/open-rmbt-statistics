package at.rtr.rmbt.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class ClassificationRequest {

    @JsonProperty(value = "count")
    @Schema(description = "Amount of classification items supported by client", example = "5")
    private final Integer count;

}
