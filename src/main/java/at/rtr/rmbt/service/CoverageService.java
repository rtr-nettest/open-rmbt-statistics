package at.rtr.rmbt.service;

import at.rtr.rmbt.response.coverage.CoverageDTO;
import at.rtr.rmbt.response.coverage.CoveragesDTO;

public interface CoverageService {
    CoveragesDTO getCoverage(Double latitude, Double longitude);
}
