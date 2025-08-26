package at.rtr.rmbt.service;



import at.rtr.rmbt.response.FencesItemDTO;

import java.util.List;
import java.util.UUID;

public interface FencesService {

    List<FencesItemDTO> getFences(UUID uuid);
}
