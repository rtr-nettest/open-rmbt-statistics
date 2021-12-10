package at.rtr.rmbt.controller;

import at.rtr.rmbt.constant.URIConstants;
import at.rtr.rmbt.request.StatisticRequest;
import at.rtr.rmbt.service.StatisticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;

    @PostMapping(URIConstants.STATISTICS)
    public String getStatistics(@Valid @RequestBody StatisticRequest statisticRequest) {
        return statisticService.getStatistics(statisticRequest);
    }
}
