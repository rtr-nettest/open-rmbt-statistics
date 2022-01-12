package at.rtr.rmbt.repository.impl;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.constant.Constants;
import at.rtr.rmbt.response.LocationGraphDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class LocationRepositoryImplTest {

    private static final String SQL_QUERY = "SELECT test_id, g.geo_lat latitude, g.geo_long longitude, g.accuracy loc_accuracy, g.bearing bearing, g.speed speed, g.provider provider, altitude, time "
            + " FROM geo_location g "
            + " WHERE g.test_id = ? and accuracy < " + Constants.RMBT_GEO_ACCURACY_DETAIL_LIMIT
            + " ORDER BY time;";

    @Mock
    private JdbcTemplate jdbcTemplate;
    @InjectMocks
    private LocationRepositoryImpl locationRepository;

    @Captor
    private ArgumentCaptor<PreparedStatementCreator> preparedStatementCreator;
    @Captor
    private ArgumentCaptor<PreparedStatementSetter> preparedStatementSetter;
    @Captor
    private ArgumentCaptor<ResultSetExtractor<List<LocationGraphDTO.LocationGraphItem>>> resultSetExtractor;

    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    @Test
    void getListResultSetExtractor_correctInvocation_LocationGraphItems() throws SQLException {
        locationRepository.getLocation(TestConstants.DEFAULT_TEST_UID, TestConstants.DEFAULT_TEST_TIME);

        verify(jdbcTemplate).query(preparedStatementCreator.capture(), preparedStatementSetter.capture(), resultSetExtractor.capture());

        preparedStatementCreator.getValue().createPreparedStatement(connection);
        verify(connection).prepareStatement(SQL_QUERY);

        preparedStatementSetter.getValue().setValues(preparedStatement);
        verify(preparedStatement).setLong(1, TestConstants.DEFAULT_TEST_UID);

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getDouble("longitude")).thenReturn(TestConstants.DEFAULT_LONGITUDE);
        when(resultSet.getDouble("latitude")).thenReturn(TestConstants.DEFAULT_LATITUDE);
        when(resultSet.getDouble("loc_accuracy")).thenReturn(TestConstants.DEFAULT_LOC_ACCURACY);
        when(resultSet.getTimestamp("time")).thenReturn(TestConstants.DEFAULT_TIMESTAMP);
        when(resultSet.getDouble("bearing")).thenReturn(TestConstants.DEFAULT_BEARING);
        when(resultSet.getDouble("speed")).thenReturn(TestConstants.DEFAULT_SPEED);
        when(resultSet.getDouble("altitude")).thenReturn(TestConstants.DEFAULT_ALTITUDE);
        when(resultSet.getString("provider")).thenReturn(TestConstants.DEFAULT_PROVIDER);
        var extractedResult = resultSetExtractor.getValue().extractData(resultSet);
        assertEquals(expectedLocationGraphItems(), extractedResult);
    }

    private List<LocationGraphDTO.LocationGraphItem> expectedLocationGraphItems() {
        LocationGraphDTO.LocationGraphItem locationGraphItem = new LocationGraphDTO.LocationGraphItem(
                TestConstants.DEFAULT_LONGITUDE,
                TestConstants.DEFAULT_LATITUDE,
                TestConstants.DEFAULT_LOC_ACCURACY,
                TestConstants.DEFAULT_TIMESTAMP,
                TestConstants.DEFAULT_BEARING,
                TestConstants.DEFAULT_SPEED,
                TestConstants.DEFAULT_ALTITUDE,
                TestConstants.DEFAULT_PROVIDER
        );
        return List.of(locationGraphItem);
    }
}