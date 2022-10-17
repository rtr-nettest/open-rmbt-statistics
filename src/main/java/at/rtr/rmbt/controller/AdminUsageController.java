package at.rtr.rmbt.controller;

import at.rtr.rmbt.constant.URIConstants;
import at.rtr.rmbt.exception.InvalidParameterException;
import at.rtr.rmbt.response.adminUsage.AdminUsageJsonResponse;
import at.rtr.rmbt.service.AdminUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class AdminUsageController {

    private final AdminUsageService adminUsageService;


    @GetMapping(URIConstants.ADMIN_USAGE_JSON)
    public AdminUsageJsonResponse getAdminUsageJson(@RequestParam(required = false, name = "month") String month,
                                                    @RequestParam(required = false, name = "year") String year,
                                                    @RequestParam(required = false, name = "statistic", defaultValue = "") List<String> statisticParam,
                                                    @RequestParam(required = false, name = "statistic[]", defaultValue = "") List<String> statisticArrayParam) {
        Integer monthNumber = null;
        Integer yearNumber = null;
        try {
            if (Objects.nonNull(month)) {
                monthNumber = Integer.parseInt(month);
                if (monthNumber > 11 || monthNumber < 0) {
                    throw new NumberFormatException();
                }
            }
            if (Objects.nonNull(year)) {
                yearNumber = Integer.parseInt(year);
                if (yearNumber < 0) {
                    throw new NumberFormatException();
                }
            }
        } catch (NumberFormatException e) {
            throw new InvalidParameterException("invalid parameters");
        }
        Set<String> statistics = new HashSet<>();
        statistics.addAll(statisticParam);
        statistics.addAll(statisticArrayParam);
        return adminUsageService.getAdminUsageJson(monthNumber, yearNumber, statistics);
    }
}
