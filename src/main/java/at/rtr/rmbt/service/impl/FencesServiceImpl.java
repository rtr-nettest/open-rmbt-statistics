package at.rtr.rmbt.service.impl;


import at.rtr.rmbt.repository.FencesRepository;
import at.rtr.rmbt.response.FencesItemDTO;
import at.rtr.rmbt.service.FencesService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FencesServiceImpl implements FencesService {

    private final FencesRepository fencesRepository;

    @Override
    public List<FencesItemDTO> getFences(UUID uuid) {
        return fencesRepository.getFences(uuid);
    }
}
