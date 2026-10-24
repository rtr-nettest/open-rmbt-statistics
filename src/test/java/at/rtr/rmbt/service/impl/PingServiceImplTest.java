package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.repository.PingRepository;
import at.rtr.rmbt.response.PingGraphItemDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PingServiceImplTest {

    @Mock
    private PingRepository pingRepository;
    @InjectMocks
    private PingServiceImpl pingService;

    @Mock
    private List<PingGraphItemDTO> pingGraphItemDTOS;

    @Test
    void getPingGraph_correctInvocation() {
        when(pingRepository.getPingGraph(TestConstants.DEFAULT_OPEN_TEST_UUID)).thenReturn(pingGraphItemDTOS);

        var actualResult = pingService.getPingGraph(TestConstants.DEFAULT_OPEN_TEST_UUID);

        assertEquals(pingGraphItemDTOS, actualResult);
    }
}