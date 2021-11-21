package com.example.statisticserver.service.impl;


import com.example.statisticserver.constant.AdminSettingConfig;
import com.example.statisticserver.constant.Constants;
import com.example.statisticserver.model.Settings;
import com.example.statisticserver.repository.SettingsRepository;
import com.example.statisticserver.response.ApplicationVersionResponse;
import com.example.statisticserver.service.ApplicationVersionService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicationVersionServiceImpl implements ApplicationVersionService {

    @Value("${git.branch}")
    private String branch;

    @Value("${git.commit.id.describe}")
    private String describe;

    @Value("${application-version.host}")
    private String applicationHost;

    private final SettingsRepository settingsRepository;

    @Override
    public ApplicationVersionResponse getApplicationVersion() {
        return ApplicationVersionResponse.builder()
                .version(String.format(Constants.VERSION_TEMPLATE, branch, describe))
                .systemUUID(getSystemUUID())
                .host(applicationHost)
                .build();
    }

    private String getSystemUUID() {
        return settingsRepository.findFirstByKeyAndLangIsNullOrKeyAndLangOrderByLang(AdminSettingConfig.SYSTEM_UUID_KEY, AdminSettingConfig.SYSTEM_UUID_KEY, StringUtils.EMPTY)
                .map(Settings::getValue)
                .orElse(null);
    }
}
