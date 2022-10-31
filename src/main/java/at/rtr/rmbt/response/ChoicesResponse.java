package at.rtr.rmbt.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Set;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class ChoicesResponse {

    @JsonProperty(value = "country_mobile")
    private final Set<String> countryMobile;
    @JsonProperty(value = "provider_mobile")
    private final Set<String> providerMobile;
    @JsonProperty(value = "provider")
    private final Set<String> provider;
}
