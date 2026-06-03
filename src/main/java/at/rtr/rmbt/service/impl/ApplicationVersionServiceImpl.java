package at.rtr.rmbt.service.impl;


import at.rtr.rmbt.constant.AdminSettingConfig;
import at.rtr.rmbt.constant.Constants;
import at.rtr.rmbt.model.Settings;
import at.rtr.rmbt.repository.SettingsRepository;
import at.rtr.rmbt.response.ApplicationVersionResponse;
import at.rtr.rmbt.service.ApplicationVersionService;
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

    @Value("${git.build.time}")
    private String buildTime;

    @Value("${application-version.host}")
    private String applicationHost;

    // get active profile or "default" if not set
    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    private final SettingsRepository settingsRepository;

    @Override
    public ApplicationVersionResponse getApplicationVersion() {
        return ApplicationVersionResponse.builder()
                .version(formatVersion(describe, branch, buildTime))
                .systemUUID(getSystemUUID())
                .host(applicationHost)
                .profile(activeProfile)
                .build();
    }

    /**
     * Builds the harmonised version string {@code <describe>(<branch>) <buildTime>}, e.g.
     * {@code v1.7.0-9-g903e63e(dev) 2026-06-02T18:35:58Z}. With the git plugin configured for a
     * "long" describe, this always carries the latest tag, the commit count since that tag, and the
     * short commit hash - even on a clean tag build.
     *
     * @param describe  the git describe (tag-N-gHASH)
     * @param branch    the git branch
     * @param buildTime the build timestamp
     * @return the formatted version string
     */
    public static String formatVersion(String describe, String branch, String buildTime) {
        return describe + "(" + formatBranch(branch) + ") " + buildTime;
    }

    private static String formatBranch(String branch) {
        // In a detached HEAD checkout (e.g. CI building a tag) git.branch is the full commit hash;
        // show "HEAD" instead of a 40-char hash so the branch field stays meaningful.
        if (branch == null || branch.isBlank() || branch.matches("[0-9a-f]{7,40}")) {
            return "HEAD";
        }
        return branch;
    }

    private String getSystemUUID() {
        return settingsRepository.findFirstByKeyAndLangIsNullOrKeyAndLangOrderByLang(AdminSettingConfig.SYSTEM_UUID_KEY, AdminSettingConfig.SYSTEM_UUID_KEY, StringUtils.EMPTY)
                .map(Settings::getValue)
                .orElse(null);
    }
}
