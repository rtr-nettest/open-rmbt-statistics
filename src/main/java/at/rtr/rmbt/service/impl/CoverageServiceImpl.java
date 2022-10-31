package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.repository.CoverageRepository;
import at.rtr.rmbt.response.coverage.CoverageDTO;
import at.rtr.rmbt.response.coverage.CoveragesDTO;
import at.rtr.rmbt.service.CoverageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoverageServiceImpl implements CoverageService {

    private final CoverageRepository coverageRepository;

    @Override
    public CoveragesDTO getCoverage(Double latitude, Double longitude) {
        long startTime = System.currentTimeMillis();
        List<CoverageDTO> coverages = coverageRepository.getCoverage(latitude, longitude);
        long elapsedTime = System.currentTimeMillis() - startTime;
        return new CoveragesDTO(coverages, elapsedTime);
    }
}
