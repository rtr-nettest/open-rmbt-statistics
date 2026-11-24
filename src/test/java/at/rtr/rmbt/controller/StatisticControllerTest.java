package at.rtr.rmbt.controller;

import at.rtr.rmbt.config.WebMvcConfig;
import at.rtr.rmbt.request.StatisticRequest;
import at.rtr.rmbt.service.StatisticService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the message-converter wiring in {@link WebMvcConfig} under Spring Boot 4. Two properties
 * must hold simultaneously:
 * <ol>
 *   <li><b>Request deserialization:</b> the Lombok @Builder/final-field request DTOs (StatisticRequest,
 *       CapabilitiesRequest, ...) must bind via Jackson 2. Boot 4's default Jackson 3 mapper cannot
 *       construct them and answers every body with 400.</li>
 *   <li><b>Response passthrough:</b> endpoints that return an already-built JSON {@code String} (this
 *       controller, /opentests/search, ...) must be written verbatim. If the Jackson 2 converter is
 *       placed before {@link StringHttpMessageConverter}, it double-encodes the body (wraps it in
 *       quotes and escapes it), so clients get a JSON string instead of an object — which looks like
 *       "empty results" in the frontend.</li>
 * </ol>
 * The test builds the converter list the way Spring MVC presents it (String converter before the JSON
 * converter), applies {@code WebMvcConfig.extendMessageConverters}, and drives the real controller.
 */
@ExtendWith(MockitoExtension.class)
class StatisticControllerTest {

    /** The exact body posted by https://www.netztest.at/ that returned HTTP 400 on Boot 4. */
    private static final String FRONTEND_PAYLOAD =
            "{\"timezone\":\"Europe/Vienna\",\"type\":\"mobile\",\"province\":null,\"end_date\":null," +
                    "\"quantile\":\"0.5\",\"network_type_group\":\"all\",\"max_devices\":100," +
                    "\"capabilities\":{\"classification\":{\"count\":4}},\"language\":\"en\"," +
                    "\"duration\":31,\"location_accuracy\":\"2000\",\"country\":null}";

    /** A service response is an already-serialized JSON string; it must reach the client verbatim. */
    private static final String SERVICE_JSON = "{\"duration\":31,\"devices\":[{\"count\":2066}]}";

    @Mock
    private StatisticService statisticService;

    @Captor
    private ArgumentCaptor<StatisticRequest> requestCaptor;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Mirror the Boot-autoconfigured Jackson 2 mapper (registers the parameter-names module, which
        // is what lets Jackson 2 construct the DTOs).
        final ObjectMapper jackson2 = Jackson2ObjectMapperBuilder.json().build();

        // Mirror the default MVC converter order that extendMessageConverters receives: the String
        // converter comes before the JSON converter. WebMvcConfig must insert Jackson 2 between them.
        final List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(new ByteArrayHttpMessageConverter());
        converters.add(new StringHttpMessageConverter());
        converters.add(new MappingJackson2HttpMessageConverter(jackson2)); // stands in for the Jackson 3 default
        new WebMvcConfig(jackson2).extendMessageConverters(converters);

        mockMvc = MockMvcBuilders.standaloneSetup(new StatisticController(statisticService))
                .setMessageConverters(converters.toArray(new HttpMessageConverter<?>[0]))
                .build();
    }

    @Test
    void postStatistics_frontendPayload_deserializesAndReturnsRawJson() throws Exception {
        when(statisticService.getStatistics(any())).thenReturn(SERVICE_JSON);

        mockMvc.perform(post("/statistics").contentType(APPLICATION_JSON).content(FRONTEND_PAYLOAD))
                .andExpect(status().isOk())
                // Must be the raw JSON object, NOT a quoted/escaped JSON string ("{\"duration\":...}").
                .andExpect(content().string(SERVICE_JSON));

        // Prove the body actually bound (not merely "did not 400"), including string->Double coercion.
        verify(statisticService).getStatistics(requestCaptor.capture());
        final StatisticRequest req = requestCaptor.getValue();
        assertEquals("mobile", req.getType());
        assertEquals("all", req.getNetworkTypeGroup());
        assertEquals(0.5d, req.getQuantile());
        assertEquals(2000d, req.getLocationAccuracy());
        assertEquals(100, req.getMaxDevices());
        assertEquals(31, req.getDuration());
        assertEquals("Europe/Vienna", req.getTimezone());
        assertEquals(4, req.getCapabilitiesRequest().getClassification().getCount());
    }
}
