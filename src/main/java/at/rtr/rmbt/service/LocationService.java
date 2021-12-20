package at.rtr.rmbt.service;

import at.rtr.rmbt.response.LocationGraphDTO;

public interface LocationService {

    LocationGraphDTO getLocationGraph(Long testUid, long time);
}
