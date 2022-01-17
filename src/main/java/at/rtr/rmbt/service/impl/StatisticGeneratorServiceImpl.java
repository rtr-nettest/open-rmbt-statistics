package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.dto.StatisticParameters;
import at.rtr.rmbt.repository.StatisticRepository;
import at.rtr.rmbt.service.StatisticGeneratorService;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
@RequiredArgsConstructor
public class StatisticGeneratorServiceImpl implements StatisticGeneratorService {

    private final StatisticRepository statisticRepository;

    @Cacheable(value = "statisticCache")
    @Override
    public String generateStatistics(StatisticParameters params, boolean ultraGreen) {
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
