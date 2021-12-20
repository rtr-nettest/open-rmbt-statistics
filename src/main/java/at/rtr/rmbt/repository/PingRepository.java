package at.rtr.rmbt.repository;

import at.rtr.rmbt.response.PingGraphItemDTO;

import java.util.List;
import java.util.UUID;

public interface PingRepository {
    List<PingGraphItemDTO> getPingGraph(UUID fromString);
}
