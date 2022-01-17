package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.config.ApplicationProperties;
import at.rtr.rmbt.constant.Constants;
import at.rtr.rmbt.dto.StatisticParameters;
import at.rtr.rmbt.request.CapabilitiesRequest;
import at.rtr.rmbt.request.ClassificationRequest;
import at.rtr.rmbt.request.StatisticRequest;
import at.rtr.rmbt.service.StatisticGeneratorService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class StatisticServiceImplTest {

    @Mock
    private ApplicationProperties applicationProperties;
    @Mock
    private StatisticGeneratorService statisticGeneratorService;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Clock clock;
    @InjectMocks
    StatisticServiceImpl statisticServiceImpl;

    @Mock
    private StatisticRequest statisticRequest;
    @Mock
    private CapabilitiesRequest capabilitiesRequest;
    @Mock
    private ClassificationRequest classificationRequest;
    @Mock
    private Cache staleCache;
    @Mock
    private Cache statisticCache;
    @Mock
    private Cache.ValueWrapper valueWrapper;

    @Test
    void getStatistics_classificationCountLess4NotStaleExpired_expectResponse() {
        Instant instantClockNow = Instant.ofEpochMilli(TestConstants.DEFAULT_TIME_LONG);
        Instant cachedInstant = Instant.ofEpochMilli(TestConstants.DEFAULT_TIME_LONG).minus(Constants.CACHE_STALE_HOURS, ChronoUnit.HOURS).plus(1, ChronoUnit.MINUTES);
        when(statisticRequest.getCapabilitiesRequest()).thenReturn(capabilitiesRequest);
        when(capabilitiesRequest.getClassification()).thenReturn(classificationRequest);
        when(classificationRequest.getCount()).thenReturn(2);
        when(applicationProperties.getDefaultLanguage()).thenReturn(TestConstants.DEFAULT_LANGUAGE);
        when(statisticGeneratorService.generateStatistics(getStatisticParameters(), false)).thenReturn(TestConstants.DEFAULT_TEXT);
        when(cacheManager.getCache(Constants.STATISTICS_STALE_CACHE_NAME)).thenReturn(staleCache);
        when(cacheManager.getCache(Constants.STATISTIC_CACHE_NAME)).thenReturn(statisticCache);
        when(staleCache.get(getSimpleKey())).thenReturn(valueWrapper);
        when(valueWrapper.get()).thenReturn(cachedInstant);
        when(clock.instant()).thenReturn(instantClockNow);

        String result = statisticServiceImpl.getStatistics(statisticRequest);
        Assertions.assertEquals(TestConstants.DEFAULT_TEXT, result);
        verify(statisticCache, times(0)).evictIfPresent(getSimpleKey());
    }

    @Test
    void getStatistics_classificationCountLess4IsStaleExpired_expectResponse() throws InterruptedException {
        Instant instantClockNow = Instant.ofEpochMilli(TestConstants.DEFAULT_TIME_LONG);
        Instant cachedInstant = Instant.ofEpochMilli(TestConstants.DEFAULT_TIME_LONG).minus(Constants.CACHE_STALE_HOURS, ChronoUnit.HOURS).minus(1, ChronoUnit.MINUTES);
        when(statisticRequest.getCapabilitiesRequest()).thenReturn(capabilitiesRequest);
        when(capabilitiesRequest.getClassification()).thenReturn(classificationRequest);
        when(classificationRequest.getCount()).thenReturn(2);
        when(applicationProperties.getDefaultLanguage()).thenReturn(TestConstants.DEFAULT_LANGUAGE);
        when(statisticGeneratorService.generateStatistics(getStatisticParameters(), false)).thenReturn(TestConstants.DEFAULT_TEXT);
        when(cacheManager.getCache(Constants.STATISTICS_STALE_CACHE_NAME)).thenReturn(staleCache);
        when(cacheManager.getCache(Constants.STATISTIC_CACHE_NAME)).thenReturn(statisticCache);
        when(staleCache.get(getSimpleKey())).thenReturn(valueWrapper);
        when(valueWrapper.get()).thenReturn(cachedInstant);
        when(clock.instant()).thenReturn(instantClockNow);

        String result = statisticServiceImpl.getStatistics(statisticRequest);
        Thread.sleep(100);
        Assertions.assertEquals(TestConstants.DEFAULT_TEXT, result);
        verify(statisticCache).evictIfPresent(getSimpleKey());
    }

    private SimpleKey getSimpleKey() {
        return new SimpleKey(getStatisticParameters(), false);
    }

    private StatisticParameters getStatisticParameters() {
        return new StatisticParameters(TestConstants.DEFAULT_LANGUAGE, statisticRequest);
    }
}