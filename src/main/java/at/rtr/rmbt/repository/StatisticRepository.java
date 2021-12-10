package at.rtr.rmbt.repository;

import at.rtr.rmbt.dto.StatisticParameters;

public interface StatisticRepository {

    String generateStatistics(StatisticParameters params, String cacheKey, boolean ultraGreen);
}
