package at.rtr.rmbt.repository;

import at.rtr.rmbt.response.coverage.CoverageDTO;

import java.util.List;

public interface CoverageRepository {
    List<CoverageDTO> getCoverage(Double latitude, Double longitude);
}
