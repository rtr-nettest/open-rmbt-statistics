package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.config.ApplicationProperties;
import at.rtr.rmbt.dto.StatisticParameters;
import at.rtr.rmbt.request.StatisticRequest;
import at.rtr.rmbt.service.StatisticGeneratorService;
import at.rtr.rmbt.service.StatisticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {

    private final ApplicationProperties applicationProperties;
    private final StatisticGeneratorService statisticGeneratorService;

    @Override
    public String getStatistics(StatisticRequest statisticRequest) {
        final boolean ultraGreen = statisticRequest.getCapabilitiesRequest().getClassification().getCount() == 4;
        final StatisticParameters params = new StatisticParameters(applicationProperties.getDefaultLanguage(), statisticRequest);
        return statisticGeneratorService.generateStatistics(params, ultraGreen);
    }
}
