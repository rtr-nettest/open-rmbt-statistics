package at.rtr.rmbt.service;

import at.rtr.rmbt.response.SignalGraphItemDTO;

import java.util.List;
import java.util.UUID;

public interface RadioSignalService {

    List<SignalGraphItemDTO> getRadioSignalGraph(Long testUid, UUID openTestUuid, long time);
}
