package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.repository.LocationRepository;
import at.rtr.rmbt.response.LocationGraphDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class LocationServiceImplTest {

    @Mock
    private LocationRepository locationRepository;
    @InjectMocks
    private LocationServiceImpl locationService;

    @Test
    void getLocationGraph_correctInvocation_LocationGraphDTO() {
        var locationGraphDTOs = givenLocationGraphItems();
        when(locationRepository.getLocation(TestConstants.DEFAULT_TEST_UID, TestConstants.DEFAULT_CLIENT_TIME_LONG))
                .thenReturn(givenLocationGraphItems());

        var actualResult = locationService.getLocationGraph(TestConstants.DEFAULT_TEST_UID, TestConstants.DEFAULT_CLIENT_TIME_LONG);
        assertEquals(expectedLocationGraphDTO(), actualResult);
    }

    private List<LocationGraphDTO.LocationGraphItem> expectedLocationGraphItems() {
        List<LocationGraphDTO.LocationGraphItem> locations = new ArrayList<>();
        LocationGraphDTO.LocationGraphItem locationSecond = new LocationGraphDTO.LocationGraphItem(
                TestConstants.DEFAULT_LONGITUDE_SECOND,
                TestConstants.DEFAULT_LATITUDE_SECOND,
                TestConstants.DEFAULT_LOC_ACCURACY_SECOND,
                new Timestamp(TestConstants.DEFAULT_CLIENT_TIME_LONG - 10),
                TestConstants.DEFAULT_BEARING_SECOND,
                TestConstants.DEFAULT_SPEED_SECOND,
                TestConstants.DEFAULT_ALTITUDE_SECOND,
                TestConstants.DEFAULT_PROVIDER_SECOND

        );
        locationSecond.setTimeElapsed(0);
        locations.add(locationSecond);
        LocationGraphDTO.LocationGraphItem locationThird = new LocationGraphDTO.LocationGraphItem(
                TestConstants.DEFAULT_LONGITUDE_THIRD,
                TestConstants.DEFAULT_LATITUDE_THIRD,
                TestConstants.DEFAULT_LOC_ACCURACY_THIRD,
                new Timestamp(TestConstants.DEFAULT_CLIENT_TIME_LONG + 10),
                TestConstants.DEFAULT_BEARING_THIRD,
                TestConstants.DEFAULT_SPEED_THIRD,
                TestConstants.DEFAULT_ALTITUDE_THIRD,
                TestConstants.DEFAULT_PROVIDER_THIRD

        );
        locationThird.setTimeElapsed(10);
        locations.add(locationThird);
        LocationGraphDTO.LocationGraphItem locationFourth = new LocationGraphDTO.LocationGraphItem(
                TestConstants.DEFAULT_LONGITUDE_FOURTH,
                TestConstants.DEFAULT_LATITUDE_FOURTH,
                TestConstants.DEFAULT_LOC_ACCURACY_FOURTH,
                new Timestamp(TestConstants.DEFAULT_CLIENT_TIME_LONG + 100),
                TestConstants.DEFAULT_BEARING_FOURTH,
                TestConstants.DEFAULT_SPEED_FOURTH,
                TestConstants.DEFAULT_ALTITUDE_FOURTH,
                TestConstants.DEFAULT_PROVIDER_FOURTH

        );
        locationFourth.setTimeElapsed(100);
        locations.add(locationFourth);
        return locations;
    }

    private List<LocationGraphDTO.LocationGraphItem> givenLocationGraphItems() {
        List<LocationGraphDTO.LocationGraphItem> locations = new ArrayList<>();
        LocationGraphDTO.LocationGraphItem locationFirst = new LocationGraphDTO.LocationGraphItem(
                TestConstants.DEFAULT_LONGITUDE,
                TestConstants.DEFAULT_LATITUDE,
                TestConstants.DEFAULT_LOC_ACCURACY,
                new Timestamp(TestConstants.DEFAULT_CLIENT_TIME_LONG - 20),
                TestConstants.DEFAULT_BEARING,
                TestConstants.DEFAULT_SPEED,
                TestConstants.DEFAULT_ALTITUDE,
                TestConstants.DEFAULT_PROVIDER

        );
        locations.add(locationFirst);
        LocationGraphDTO.LocationGraphItem locationSecond = new LocationGraphDTO.LocationGraphItem(
                TestConstants.DEFAULT_LONGITUDE_SECOND,
                TestConstants.DEFAULT_LATITUDE_SECOND,
                TestConstants.DEFAULT_LOC_ACCURACY_SECOND,
                new Timestamp(TestConstants.DEFAULT_CLIENT_TIME_LONG - 10),
                TestConstants.DEFAULT_BEARING_SECOND,
                TestConstants.DEFAULT_SPEED_SECOND,
                TestConstants.DEFAULT_ALTITUDE_SECOND,
                TestConstants.DEFAULT_PROVIDER_SECOND

        );
        locations.add(locationSecond);
        LocationGraphDTO.LocationGraphItem locationThird = new LocationGraphDTO.LocationGraphItem(
                TestConstants.DEFAULT_LONGITUDE_THIRD,
                TestConstants.DEFAULT_LATITUDE_THIRD,
                TestConstants.DEFAULT_LOC_ACCURACY_THIRD,
                new Timestamp(TestConstants.DEFAULT_CLIENT_TIME_LONG + 10),
                TestConstants.DEFAULT_BEARING_THIRD,
                TestConstants.DEFAULT_SPEED_THIRD,
                TestConstants.DEFAULT_ALTITUDE_THIRD,
                TestConstants.DEFAULT_PROVIDER_THIRD

        );
        locations.add(locationThird);
        LocationGraphDTO.LocationGraphItem locationFourth = new LocationGraphDTO.LocationGraphItem(
                TestConstants.DEFAULT_LONGITUDE_FOURTH,
                TestConstants.DEFAULT_LATITUDE_FOURTH,
                TestConstants.DEFAULT_LOC_ACCURACY_FOURTH,
                new Timestamp(TestConstants.DEFAULT_CLIENT_TIME_LONG + 100),
                TestConstants.DEFAULT_BEARING_FOURTH,
                TestConstants.DEFAULT_SPEED_FOURTH,
                TestConstants.DEFAULT_ALTITUDE_FOURTH,
                TestConstants.DEFAULT_PROVIDER_FOURTH

        );
        locations.add(locationFourth);
        return locations;
    }

    private LocationGraphDTO expectedLocationGraphDTO() {
        LocationGraphDTO locationGraphDTO = new LocationGraphDTO();
        locationGraphDTO.setLocations(expectedLocationGraphItems());
        locationGraphDTO.setTotalDistance(TestConstants.DEFAULT_TOTAL_DISTANCE);
        return locationGraphDTO;
    }
}