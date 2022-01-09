package at.rtr.rmbt.repository.impl;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.dto.TestDistance;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class GeoAnalyticsRepositoryImplTest {

    private static final String SQL_QUERY = "select max(g.accuracy) max_accuracy,st_lengthSpheroid(st_transform(st_makeline(g.location " +
            "order by g.time_ns),4326),'SPHEROID[\"WGS 84\",6378137,298.257223563]') as distance " +
            "from geo_location as g  where g.open_test_uuid= ? and (g.provider='gps' or g.provider='' or g.provider is null) " +
            "group by g.open_test_uuid;";

    @Mock
    private JdbcTemplate jdbcTemplate;
    @InjectMocks
    private GeoAnalyticsRepositoryImpl geoAnalyticsRepository;

    @Captor
    private ArgumentCaptor<PreparedStatementCreator> preparedStatementCreator;
    @Captor
    private ArgumentCaptor<PreparedStatementSetter> preparedStatementSetter;
    @Captor
    private ArgumentCaptor<ResultSetExtractor<TestDistance>> resultSetExtractor;

    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    @Test
    void getTestDistance_correctInvocation_TestDistance() throws SQLException {
        geoAnalyticsRepository.getTestDistance(TestConstants.DEFAULT_OPEN_TEST_UUID);

        verify(jdbcTemplate).query(preparedStatementCreator.capture(), preparedStatementSetter.capture(), resultSetExtractor.capture());

        preparedStatementCreator.getValue().createPreparedStatement(connection);
        verify(connection).prepareStatement(SQL_QUERY);

        preparedStatementSetter.getValue().setValues(preparedStatement);
        verify(preparedStatement).setObject(1, TestConstants.DEFAULT_OPEN_TEST_UUID, Types.OTHER);

        when(resultSet.getLong("distance")).thenReturn(TestConstants.DEFAULT_TEST_DISTANCE);
        when(resultSet.getLong("max_accuracy")).thenReturn(TestConstants.DEFAULT_TEST_MAX_ACCURACY);
        when(resultSet.next()).thenReturn(true);
        var extractedResult = resultSetExtractor.getValue().extractData(resultSet);
        assertEquals(expectedTestDistance(), extractedResult);
    }

    @Test
    void getTestDistance_whenResultSetEmpty_TestDistance() throws SQLException {
        geoAnalyticsRepository.getTestDistance(TestConstants.DEFAULT_OPEN_TEST_UUID);

        verify(jdbcTemplate).query(preparedStatementCreator.capture(), preparedStatementSetter.capture(), resultSetExtractor.capture());

        when(resultSet.next()).thenReturn(false);
        var extractedResult = resultSetExtractor.getValue().extractData(resultSet);
        assertEquals(expectedTestDistanceZero(), extractedResult);
    }

    private TestDistance expectedTestDistance() {
        return new TestDistance(TestConstants.DEFAULT_TEST_DISTANCE, TestConstants.DEFAULT_TEST_MAX_ACCURACY);
    }

    private TestDistance expectedTestDistanceZero() {
        return new TestDistance(0L, 0L);
    }
}