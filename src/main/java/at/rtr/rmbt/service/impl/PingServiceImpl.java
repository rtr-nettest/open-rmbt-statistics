package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.repository.PingRepository;
import at.rtr.rmbt.response.PingGraphItemDTO;
import at.rtr.rmbt.service.PingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PingServiceImpl implements PingService {

    private final PingRepository pingRepository;

    @Override
    public List<PingGraphItemDTO> getPingGraph(UUID uuid) {
        return pingRepository.getPingGraph(uuid);
    }
}
