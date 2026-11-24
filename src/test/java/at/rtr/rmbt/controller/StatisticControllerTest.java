package at.rtr.rmbt.controller;

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
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that POST /statistics deserializes the production frontend payload through a Jackson 2
 * message converter (as wired in {@link at.rtr.rmbt.config.WebMvcConfig} under Spring Boot 4). The
 * request DTOs are Lombok @Builder/final-field classes that Jackson 2 binds via implicit
 * single-constructor detection; Boot 4's default Jackson 3 mapper cannot construct them and answers
 * every body with 400. This test reproduces the exact body that failed in production, including the
 * {@code "0.5"} / {@code "2000"} string-to-Double coercion that Jackson 2 performs leniently.
 */
@ExtendWith(MockitoExtension.class)
class StatisticControllerTest {

    /** The exact body posted by https://www.netztest.at/ that returned HTTP 400 on Boot 4. */
    private static final String FRONTEND_PAYLOAD =
            "{\"timezone\":\"Europe/Vienna\",\"type\":\"mobile\",\"province\":null,\"end_date\":null," +
                    "\"quantile\":\"0.5\",\"network_type_group\":\"all\",\"max_devices\":100," +
                    "\"capabilities\":{\"classification\":{\"count\":4}},\"language\":\"en\"," +
                    "\"duration\":31,\"location_accuracy\":\"2000\",\"country\":null}";

    @Mock
    private StatisticService statisticService;

    @Captor
    private ArgumentCaptor<StatisticRequest> requestCaptor;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Mirror the Boot-autoconfigured Jackson 2 mapper: registers the parameter-names module
        // (classes compiled with -parameters), which is what lets Jackson 2 construct the DTOs.
        final ObjectMapper jackson2 = Jackson2ObjectMapperBuilder.json().build();
        mockMvc = MockMvcBuilders.standaloneSetup(new StatisticController(statisticService))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(jackson2))
                .build();
    }

    @Test
    void postStatistics_frontendPayload_deserializesAndReturns200() throws Exception {
        when(statisticService.getStatistics(any())).thenReturn("{\"result\":\"ok\"}");

        mockMvc.perform(post("/statistics").contentType(APPLICATION_JSON).content(FRONTEND_PAYLOAD))
                .andExpect(status().isOk());

        // Prove the body actually bound (not merely "did not 400"), including string->Double coercion.
        verifyBoundRequest();
    }

    private void verifyBoundRequest() {
        org.mockito.Mockito.verify(statisticService).getStatistics(requestCaptor.capture());
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
