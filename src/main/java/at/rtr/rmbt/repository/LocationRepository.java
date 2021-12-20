package at.rtr.rmbt.repository;

import at.rtr.rmbt.response.LocationGraphDTO;

import java.util.List;

public interface LocationRepository {

    List<LocationGraphDTO.LocationGraphItem> getLocation(Long testUid, long time);
}
