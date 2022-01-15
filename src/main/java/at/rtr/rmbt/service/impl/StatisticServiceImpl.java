package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.config.ApplicationProperties;
import at.rtr.rmbt.dto.StatisticParameters;
import at.rtr.rmbt.repository.StatisticRepository;
import at.rtr.rmbt.request.StatisticRequest;
import at.rtr.rmbt.service.StatisticService;
import at.rtr.rmbt.utils.CacheHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {
    private static final int CACHE_STALE = 3600; //1 hour
    private static final int CACHE_EXPIRE = 21600; //6 hours
    //put last 10.000 request entities into a cache :) (O(10KB*1000=10MB))
    private static final LinkedBlockingQueue<String> lastRequests = new LinkedBlockingQueue<>(10000);

    private static final CacheHelper cache = CacheHelper.getInstance();

    private final ApplicationProperties applicationProperties;
    private final StatisticRepository statisticRepository;

    @Override
    @Cacheable(value = "itemCache")
    public String getStatistics(StatisticRequest statisticRequest) {
        String entity = statisticRequest.toString();
        final boolean ultraGreen = statisticRequest.getCapabilitiesRequest().getClassification().getCount() == 4;

        //add to last requests
        boolean success = lastRequests.offer(entity);
        if (!success) {
            lastRequests.poll();
        }

        return generateStatistics(statisticRequest, ultraGreen);
    }


    private String generateStatistics(StatisticRequest statisticRequest, boolean ultraGreen) {
        final StatisticParameters params = new StatisticParameters(applicationProperties.getDefaultLanguage(), statisticRequest);
        final String cacheKey = CacheHelper.getHash(params);
        final CacheHelper.ObjectWithTimestamp cacheObject = cache.getWithTimestamp(cacheKey, CACHE_STALE);

        if (cacheObject != null) {//TODO: CACHE
            final String result = (String) cacheObject.o;
            log.info("cache hit");
            if (cacheObject.stale) {
                final Runnable refreshCacheRunnable = () -> {
                    log.info("adding in background: " + cacheKey);
                    final String result1 = generateStatistics(params, cacheKey, ultraGreen);
                    if (result1 != null)
                        cache.set(cacheKey, CACHE_EXPIRE, result1, true);
                };
                cache.getExecutor().execute(refreshCacheRunnable);
            }
            return result; // cache hit
        }
        log.info("not in cache");

        final String result = generateStatistics(params, cacheKey, ultraGreen);
        if (result != null)
            cache.set(cacheKey, CACHE_EXPIRE, result, true);
        return result;
    }

    private String generateStatistics(StatisticParameters params, String cacheKey, boolean ultraGreen) {
        String result;
        final String lang = params.getLang();
        final float quantile = params.getQuantile();
        final int durationDays = params.getDuration();
        final int maxDevices = params.getMaxDevices();
        final String type = params.getType();
        final String networkTypeGroup = params.getNetworkTypeGroup();
        final double accuracy = params.getAccuracy();
        final String country = params.getCountry();
        final java.sql.Timestamp endDate = params.getEndDate();
        final int province = params.getProvince();

        final boolean userServerSelection = params.isUserServerSelection();

        final JSONObject answer = new JSONObject();

        try {
            final JSONArray providers = new JSONArray();
            answer.put("providers", providers);
            final JSONArray devices = new JSONArray();
            answer.put("devices", devices);
            answer.put("quantile", quantile);
            answer.put("duration", durationDays);
            answer.put("type", type);
            final JSONArray providersArray = statisticRepository.selectProviders(lang, true, quantile, durationDays, accuracy, country,
                    type, networkTypeGroup, userServerSelection, endDate, province, ultraGreen);
            providers.putAll(providersArray);
            final JSONArray providersSumsArray = statisticRepository.selectProviders(lang, false, quantile, durationDays, accuracy, country,
                    type, networkTypeGroup, userServerSelection, endDate, province, ultraGreen);
            if (providersSumsArray.length() == 1)
                answer.put("providers_sums", providersSumsArray.get(0));

            devices.putAll(statisticRepository.selectDevices(lang, true, quantile, durationDays, accuracy, country,
                    type, networkTypeGroup, maxDevices, userServerSelection, endDate, province));

            final JSONArray devicesSumsArray = statisticRepository.selectDevices(lang, false, quantile, durationDays, accuracy, country,
                    type, networkTypeGroup, maxDevices, userServerSelection, endDate, province);
            if (devicesSumsArray.length() == 1)
                answer.put("devices_sums", devicesSumsArray.get(0));

            final JSONArray countries = new JSONArray(statisticRepository.getCountries());
            answer.put("countries", countries);

            result = answer.toString();
            return result;


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


        final JSONArray countries = new JSONArray(statisticRepository.getCountries());
        answer.put("countries", countries);

        result = answer.toString();
        return result;
    }
}
