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
//        if (!rsSignal.isBeforeFirst()) {
//            psSignal.close();
//            additionalInformation = false;
//
//            psSignal = conn.prepareStatement("SELECT test_id, nt.name network_type, nt.group_name cat_technology, signal_strength, lte_rsrp, lte_rsrq, wifi_rssi, time "
//                    + "FROM signal "
//                    + "JOIN network_type nt "
//                    + "ON nt.uid = network_type_id "
//                    + "WHERE open_test_uuid = ? "
//                    + "ORDER BY time;");
//            psSignal.setObject(1, openTestUuid);
//
//            rsSignal = psSignal.executeQuery();
//
//        }
        return signalList;
    }
}
