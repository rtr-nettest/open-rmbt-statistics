package at.rtr.rmbt.controller;

import at.rtr.rmbt.constant.URIConstants;
import at.rtr.rmbt.request.StatisticRequest;
import at.rtr.rmbt.service.StatisticService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Slf4j
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;

    @ApiOperation(value = "Statistics",
            nickname = "statistics")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "lang", value = "The language that should be exported.", dataType = "string", example = "en", paramType = "body", required = false),
            @ApiImplicitParam(name = "quantile", value = "Quantile", dataType = "double", example = "0.5", paramType = "body", required = false),
            @ApiImplicitParam(name = "month", value = "The month that should be calculated.", dataType = "int", example = "1", paramType = "body", required = false),
            @ApiImplicitParam(name = "duration", value = "The duration that should be calculated.", dataType = "int", example = "1", paramType = "body", required = false),
            @ApiImplicitParam(name = "max_devices", value = "The number of device that should be calculated.", dataType = "int", example = "1", paramType = "body", required = false),
            @ApiImplicitParam(name = "type", value = "The type of measurement", dataType = "string", example = "mobile", paramType = "body", required = false),
            @ApiImplicitParam(name = "network_type_group", value = "Type of the network, e.g. MOBILE, LAN, WLAN.", dataType = "string", paramType = "body", required = false),
            @ApiImplicitParam(name = "location_accuracy", value = "Estimation of accuracy of client location in meters", dataType = "string", paramType = "body", required = false),
            @ApiImplicitParam(name = "country", value = "The country of measurement in ISO 3166.", dataType = "string", example = "en", paramType = "body", required = false),
            @ApiImplicitParam(name = "user_server_selection", value = "Legacy", dataType = "boolean", paramType = "body", required = false),
            @ApiImplicitParam(name = "endDate", value = "UTC date and time when calculation was ended yyyy-MM-dd HH:mm:ss", dataType = "string", paramType = "body", required = false),
            @ApiImplicitParam(name = "province", value = "Code of province gkz bev", dataType = "int", paramType = "body", required = false),
            @ApiImplicitParam(name = "timezone", value = "Timezone", example = "Europe/Vienna", dataType = "string", paramType = "body", required = false)
    })
    @PostMapping(value = URIConstants.STATISTICS, produces = "application/json; charset=utf-8")
    public String getStatistics(@Valid @RequestBody StatisticRequest statisticRequest) {
        return statisticService.getStatistics(statisticRequest);
    }
}
