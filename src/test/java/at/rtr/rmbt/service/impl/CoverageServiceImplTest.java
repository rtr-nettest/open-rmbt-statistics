package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.repository.CoverageRepository;
import at.rtr.rmbt.response.coverage.CoverageDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class CoverageServiceImplTest {

    @Mock
    private CoverageRepository coverageRepository;
    @InjectMocks
    private CoverageServiceImpl coverageService;
    @Mock
    private CoverageDTO coverageDto;

    @Test
    void getCoverage_correctInvocation_expectCoverageDTO() {
        when(coverageRepository.getCoverage(TestConstants.DEFAULT_LATITUDE, TestConstants.DEFAULT_LONGITUDE))
                .thenAnswer((Answer<List<CoverageDTO>>) invocation -> {
                    Thread.sleep(100);
                    return List.of(coverageDto);
                });

        var result = coverageService.getCoverage(TestConstants.DEFAULT_LATITUDE, TestConstants.DEFAULT_LONGITUDE);

        assertEquals(List.of(coverageDto), result.getCoverages());
        assertTrue(result.getDurationMs() > 0);
    }
}