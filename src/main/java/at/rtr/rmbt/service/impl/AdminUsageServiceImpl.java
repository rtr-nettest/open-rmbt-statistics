package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.repository.AdminUsageRepository;
import at.rtr.rmbt.response.adminUsage.AdminUsageJsonResponse;
import at.rtr.rmbt.response.adminUsage.SumsAndValuesResponse;
import at.rtr.rmbt.service.AdminUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminUsageServiceImpl implements AdminUsageService {

    private final AdminUsageRepository adminUsageRepository;

    @Override
    public AdminUsageJsonResponse getAdminUsageJson(Integer month, Integer year, Set<String> statistics) {
        Calendar now = new GregorianCalendar();
        Calendar monthBegin = new GregorianCalendar(Objects.nonNull(year) ? year : now.get(Calendar.YEAR), Objects.nonNull(month) ? month : now.get(Calendar.MONTH), 1);
        Calendar monthEnd = new GregorianCalendar(Objects.nonNull(year) ? year : now.get(Calendar.YEAR), Objects.nonNull(month) ? month : now.get(Calendar.MONTH), monthBegin.getActualMaximum(Calendar.DAY_OF_MONTH));
        //if now -> do not use the last day
        if (Objects.equals(month, now.get(Calendar.MONTH)) && Objects.equals(year, now.get(Calendar.YEAR))) {
            monthEnd = now;
            monthEnd.add(Calendar.DATE, -1);
        }

        Set<String> searchStatistics = new HashSet<>(Arrays.asList("platforms", "platforms_loopmode", "usage", "versions_ios",
                "versions_android", "versions_applet", "network_group_names", "network_group_types"));
        if (!CollectionUtils.isEmpty(statistics)) {
            searchStatistics.clear();
            searchStatistics.addAll(statistics);
        }

        AdminUsageJsonResponse.AdminUsageJsonResponseBuilder resultBuilder = AdminUsageJsonResponse.builder();
        if (searchStatistics.contains("platforms")) {
            SumsAndValuesResponse platforms = adminUsageRepository.getPlatforms(new Timestamp(monthBegin.getTimeInMillis()), new Timestamp(monthEnd.getTimeInMillis()));
            resultBuilder.platforms(platforms);
        }
        if (searchStatistics.contains("platforms_loopmode")) {
            SumsAndValuesResponse platformsLoopmode = adminUsageRepository.getLoopModePlatforms(new Timestamp(monthBegin.getTimeInMillis()), new Timestamp(monthEnd.getTimeInMillis()));
            resultBuilder.platformsLoopMode(platformsLoopmode);
        }
        if (searchStatistics.contains("usage")) {
            SumsAndValuesResponse usage = adminUsageRepository.getClassicUsage(new Timestamp(monthBegin.getTimeInMillis()), new Timestamp(monthEnd.getTimeInMillis()));
            resultBuilder.usage(usage);
        }
        if (searchStatistics.contains("versions_ios")) {
            SumsAndValuesResponse versionsIOS = adminUsageRepository.getVersions("iOS", new Timestamp(monthBegin.getTimeInMillis()), new Timestamp(monthEnd.getTimeInMillis()));
            resultBuilder.versionsIos(versionsIOS);
        }
        if (searchStatistics.contains("versions_android")) {
            SumsAndValuesResponse versionsAndroid = adminUsageRepository.getVersions("Android", new Timestamp(monthBegin.getTimeInMillis()), new Timestamp(monthEnd.getTimeInMillis()));
            resultBuilder.versionsAndroid(versionsAndroid);
        }
        if (searchStatistics.contains("versions_applet")) {
            SumsAndValuesResponse versionsApplet = adminUsageRepository.getVersions("Applet", new Timestamp(monthBegin.getTimeInMillis()), new Timestamp(monthEnd.getTimeInMillis()));
            resultBuilder.versionsApplet(versionsApplet);
        }
        if (searchStatistics.contains("network_group_names")) {
            SumsAndValuesResponse networkGroupNames = adminUsageRepository.getNetworkGroupName(new Timestamp(monthBegin.getTimeInMillis()), new Timestamp(monthEnd.getTimeInMillis()));
            resultBuilder.networkGroupNames(networkGroupNames);
        }
        if (searchStatistics.contains("network_group_types")) {
            SumsAndValuesResponse networkGroupTypes = adminUsageRepository.getNetworkGroupType(new Timestamp(monthBegin.getTimeInMillis()), new Timestamp(monthEnd.getTimeInMillis()));
            resultBuilder.networkGroupTypes(networkGroupTypes);
        }
        if (searchStatistics.contains("platforms_qos")) {
            SumsAndValuesResponse platformsQoS = adminUsageRepository.getQoSUsage(new Timestamp(monthBegin.getTimeInMillis()), new Timestamp(monthEnd.getTimeInMillis()));
            resultBuilder.platformsQos(platformsQoS);
        }

        return resultBuilder.build();
    }
}
