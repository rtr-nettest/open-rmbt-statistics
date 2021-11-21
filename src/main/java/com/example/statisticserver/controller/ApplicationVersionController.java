package com.example.statisticserver.controller;


import com.example.statisticserver.constant.URIConstants;
import com.example.statisticserver.response.ApplicationVersionResponse;
import com.example.statisticserver.service.ApplicationVersionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Api("Version")
@RestController
@RequiredArgsConstructor
public class ApplicationVersionController {

    private final ApplicationVersionService applicationVersionService;

    @ApiOperation(value = "Get version of application")
    @GetMapping(URIConstants.VERSION)
    public ApplicationVersionResponse getApplicationVersion() {
        return applicationVersionService.getApplicationVersion();
    }
}
