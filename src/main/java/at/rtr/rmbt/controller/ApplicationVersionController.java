package at.rtr.rmbt.controller;


import at.rtr.rmbt.response.ApplicationVersionResponse;
import at.rtr.rmbt.service.ApplicationVersionService;
import at.rtr.rmbt.constant.URIConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Api("Version")
@RestController
@RequiredArgsConstructor
@Slf4j
public class ApplicationVersionController {

    private final ApplicationVersionService applicationVersionService;

    @ApiOperation(value = "Get version of application")
    @GetMapping(URIConstants.VERSION)
    public ApplicationVersionResponse getApplicationVersion() {
        log.info("Started /version endpoint");
        return applicationVersionService.getApplicationVersion();
    }
}
