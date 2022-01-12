package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.repository.RadioSignalRepository;
import at.rtr.rmbt.response.SignalGraphItemDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class RadioSignalServiceImplTest {

    @Mock
    private RadioSignalRepository radioSignalRepository;
    @InjectMocks
    private RadioSignalServiceImpl radioSignalService;

    @Mock
    private List<SignalGraphItemDTO> signalGraphItemDTOs;

    @Test
    void getRadioSignalGraph_correctInvocation_SignalGraphItemDTOs() {
        when(radioSignalRepository.getSignals(TestConstants.DEFAULT_OPEN_TEST_UUID, TestConstants.DEFAULT_CLIENT_TIME_LONG))
                .thenReturn(signalGraphItemDTOs);

        var actualResult = radioSignalService.getRadioSignalGraph(TestConstants.DEFAULT_OPEN_TEST_UUID, TestConstants.DEFAULT_CLIENT_TIME_LONG);

        assertEquals(signalGraphItemDTOs, actualResult);
    }

    @Test
    void getRadioSignalGraph_legacyMethod_SignalGraphItemDTOs() {
        when(radioSignalRepository.getSignals(TestConstants.DEFAULT_OPEN_TEST_UUID, TestConstants.DEFAULT_CLIENT_TIME_LONG))
                .thenReturn(Collections.emptyList());
        when(radioSignalRepository.getSignalsLegacy(TestConstants.DEFAULT_OPEN_TEST_UUID, TestConstants.DEFAULT_CLIENT_TIME_LONG))
                .thenReturn(signalGraphItemDTOs);

        var actualResult = radioSignalService.getRadioSignalGraph(TestConstants.DEFAULT_OPEN_TEST_UUID, TestConstants.DEFAULT_CLIENT_TIME_LONG);

        assertEquals(signalGraphItemDTOs, actualResult);
    }
}