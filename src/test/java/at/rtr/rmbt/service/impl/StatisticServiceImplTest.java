package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.config.ApplicationProperties;
import at.rtr.rmbt.dto.StatisticParameters;
import at.rtr.rmbt.request.CapabilitiesRequest;
import at.rtr.rmbt.request.ClassificationRequest;
import at.rtr.rmbt.request.StatisticRequest;
import at.rtr.rmbt.service.StatisticGeneratorService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class StatisticServiceImplTest {

    @Mock
    ApplicationProperties applicationProperties;
    @Mock
    StatisticGeneratorService statisticGeneratorService;
    @InjectMocks
    StatisticServiceImpl statisticServiceImpl;

    @Mock
    private StatisticRequest statisticRequest;
    @Mock
    private CapabilitiesRequest capabilitiesRequest;
    @Mock
    private ClassificationRequest classificationRequest;

    @Test
    void getStatistics_classificationCountLess4_expectResponse() {
        when(statisticRequest.getCapabilitiesRequest()).thenReturn(capabilitiesRequest);
        when(capabilitiesRequest.getClassification()).thenReturn(classificationRequest);
        when(classificationRequest.getCount()).thenReturn(2);
        when(applicationProperties.getDefaultLanguage()).thenReturn(TestConstants.DEFAULT_LANGUAGE);
        when(statisticGeneratorService.generateStatistics(getStatisticParameters(), false)).thenReturn(TestConstants.DEFAULT_TEXT);

        String result = statisticServiceImpl.getStatistics(statisticRequest);
        Assertions.assertEquals(TestConstants.DEFAULT_TEXT, result);
    }

    private StatisticParameters getStatisticParameters() {
        return new StatisticParameters(TestConstants.DEFAULT_LANGUAGE, statisticRequest);
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme