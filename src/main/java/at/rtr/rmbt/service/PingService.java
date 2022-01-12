package at.rtr.rmbt.service;

import at.rtr.rmbt.response.PingGraphItemDTO;

import java.util.List;
import java.util.UUID;

public interface PingService {

    List<PingGraphItemDTO> getPingGraph(UUID uuid);
}
