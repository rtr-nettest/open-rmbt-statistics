package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.config.ApplicationProperties;
import at.rtr.rmbt.constant.Constants;
import at.rtr.rmbt.dto.StatisticParameters;
import at.rtr.rmbt.request.StatisticRequest;
import at.rtr.rmbt.service.StatisticGeneratorService;
import at.rtr.rmbt.service.StatisticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {

    private final ApplicationProperties applicationProperties;
    private final StatisticGeneratorService statisticGeneratorService;
    private final CacheManager cacheManager;
    private final Clock clock;

    @Override
    public String getStatistics(StatisticRequest statisticRequest) {
        final boolean ultraGreen = statisticRequest.getCapabilitiesRequest().getClassification().getCount() == 4;
        final StatisticParameters params = new StatisticParameters(applicationProperties.getDefaultLanguage(), statisticRequest);
        SimpleKey key = new SimpleKey(params, ultraGreen);

        String result = statisticGeneratorService.generateStatistics(params, ultraGreen);
        cacheManager.getCache(Constants.STATISTICS_STALE_CACHE_NAME).putIfAbsent(key, clock.instant());
        Thread thread = new Thread(() -> {
            Instant cachedInstant = (Instant) cacheManager.getCache(Constants.STATISTICS_STALE_CACHE_NAME).get(key).get();
            if (clock.instant().minus(Constants.CACHE_STALE_HOURS, ChronoUnit.HOURS).isAfter(cachedInstant)) {
                cacheManager.getCache(Constants.STATISTIC_CACHE_NAME).evictIfPresent(key);
                statisticGeneratorService.generateStatistics(params, ultraGreen);
                cacheManager.getCache(Constants.STATISTICS_STALE_CACHE_NAME).put(key, clock.instant());
            }
        });
        thread.start();

        return result;
    }
}
