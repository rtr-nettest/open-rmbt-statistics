package at.rtr.rmbt.service;

import at.rtr.rmbt.dto.StatisticParameters;

public interface StatisticGeneratorService {

    String generateStatistics(StatisticParameters params, boolean ultraGreen);
}
