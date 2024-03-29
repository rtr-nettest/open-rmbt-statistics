package at.rtr.rmbt.controller;

import at.rtr.rmbt.constant.URIConstants;
import at.rtr.rmbt.request.StatisticRequest;
import at.rtr.rmbt.service.StatisticService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;

    @Operation(summary = "Statistics",
            operationId = "statistics")
    @PostMapping(value = URIConstants.STATISTICS, produces = "application/json; charset=utf-8")
    public String getStatistics(@Valid @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody StatisticRequest statisticRequest) {
        return statisticService.getStatistics(statisticRequest);
    }
}
