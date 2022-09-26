package at.rtr.rmbt.controller;

import at.rtr.rmbt.constant.URIConstants;
import at.rtr.rmbt.exception.InvalidParameterException;
import at.rtr.rmbt.response.coverage.CoveragesDTO;
import at.rtr.rmbt.service.CoverageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CoverageController {

    private final CoverageService coverageService;

    @Operation(operationId = "coverage",
            summary = "Get coverage information",
            description = "Get coverage information for a specific point")
    @RequestMapping(value = URIConstants.COVERAGE, method = {RequestMethod.GET, RequestMethod.POST})
    @Parameter(name = "lat",
            description = "Mandatory. Latitude of the client position.",
            schema = @Schema(implementation = Double.class),
            example = "18.2345",
            in = ParameterIn.QUERY,
            required = true)
    @Parameter(name = "long",
            description = "Mandatory. Longitude of the client position.",
            schema = @Schema(implementation = Double.class),
            example = "43.1234",
            in = ParameterIn.QUERY,
            required = true)
    public CoveragesDTO getCoverage(@RequestParam(name = "lat") Double latitude,
                                    @RequestParam(name = "long") Double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new InvalidParameterException("invalid parameters");
        }
        if (longitude < -180 || longitude > 180) {
            throw new InvalidParameterException("invalid parameters");
        }
        return coverageService.getCoverage(latitude, longitude);
    }
}
