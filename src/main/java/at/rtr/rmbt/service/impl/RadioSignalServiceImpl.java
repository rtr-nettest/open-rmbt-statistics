package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.repository.RadioSignalRepository;
import at.rtr.rmbt.response.SignalGraphItemDTO;
import at.rtr.rmbt.service.RadioSignalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RadioSignalServiceImpl implements RadioSignalService {

    private final RadioSignalRepository radioSignalRepository;

    @Override
    public List<SignalGraphItemDTO> getRadioSignalGraph(Long testUid, UUID openTestUuid, long time) {

        List<SignalGraphItemDTO> signalList = radioSignalRepository.getSignals(openTestUuid, time);

        if (signalList.isEmpty()) {
            signalList = radioSignalRepository.getSignalsLegacy(openTestUuid, time);
        }

        return signalList;
    }
}
