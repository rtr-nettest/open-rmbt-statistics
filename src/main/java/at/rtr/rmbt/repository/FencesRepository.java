package at.rtr.rmbt.repository;

import at.rtr.rmbt.response.FencesItemDTO;

import java.util.List;
import java.util.UUID;

public interface FencesRepository {
    List<FencesItemDTO> getFences(UUID fromString);
}
