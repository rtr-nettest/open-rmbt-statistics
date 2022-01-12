package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.repository.PingRepository;
import at.rtr.rmbt.response.PingGraphItemDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
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