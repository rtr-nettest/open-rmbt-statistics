package at.rtr.rmbt.repository.impl;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.TestObjects;
import at.rtr.rmbt.response.HourlyStatisticResponse;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class HourlyStatisticRepositoryImplTest {

    private static final String SQL_QUERY = "SELECT  count(t.open_test_uuid),  " +
            "extract(hour from t.time AT TIME ZONE t.timezone) AS hour," +
            "       percentile_disc(?) WITHIN GROUP (ORDER BY t.speed_download :: bigint) AS quantile_down," +
            "       percentile_disc(?) WITHIN GROUP (ORDER BY t.speed_upload :: bigint) AS quantile_up," +
            "       percentile_disc(?) WITHIN GROUP (ORDER BY t.ping_median :: bigint) AS quantile_ping " +
            "FROM test t  LEFT JOIN network_type nt ON nt.uid=t.network_type " +
            "LEFT JOIN test_loopmode l ON (l.test_uuid = t.uuid) " +
            "LEFT JOIN device_map adm ON adm.codename=t.model " +
            "LEFT JOIN test_server ts ON ts.uid=t.server_id " +
            "LEFT JOIN provider prov ON provider_id = prov.uid  " +
            "LEFT JOIN provider mprov ON mobile_provider_id = mprov.uid " +
            "LEFT JOIN mccmnc2name msim ON mobile_sim_id = msim.uid  " +
            "LEFT JOIN client c ON client_id = c.uid  " +
            "LEFT JOIN test_location tl ON t.open_test_uuid = tl.open_test_uuid " +
            "WHERE t.deleted = false " +
            "AND status = 'FINISHED'  " +
            "AND country_location ILIKE ? " +
            "AND t.speed_download = ? " +
            "AND t.implausible = ?  GROUP BY hour;";

    @Mock
    private JdbcTemplate jdbcTemplate;
    @InjectMocks
    private HourlyStatisticRepositoryImpl hourlyStatisticRepository;

    @Captor
    private ArgumentCaptor<PreparedStatementCreator> preparedStatementCreator;
    @Captor
    private ArgumentCaptor<PreparedStatementSetter> preparedStatementSetter;
    @Captor
    private ArgumentCaptor<ResultSetExtractor<List<HourlyStatisticResponse>>> resultSetExtractor;

    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    @Test
    void getHourlyStatistic_correctInvocation_HourlyStatisticResponses() throws SQLException {
        var expectedResult = List.of(TestObjects.hourlyStatisticResponse(), TestObjects.hourlyStatisticResponse());
        hourlyStatisticRepository.getHourlyStatistic(TestObjects.queryParser(), TestConstants.DEFAULT_QUANTILE);

        verify(jdbcTemplate).query(preparedStatementCreator.capture(), preparedStatementSetter.capture(), resultSetExtractor.capture());

        preparedStatementCreator.getValue().createPreparedStatement(connection);
        verify(connection).prepareStatement(SQL_QUERY);

        preparedStatementSetter.getValue().setValues(preparedStatement);
        verify(preparedStatement).setDouble(1, TestConstants.DEFAULT_QUANTILE);
        verify(preparedStatement).setDouble(2, TestConstants.DEFAULT_QUANTILE);
        verify(preparedStatement).setDouble(3, TestConstants.DEFAULT_QUANTILE);

        when(resultSet.getDouble("quantile_down")).thenReturn(TestConstants.DEFAULT_QUANTILE_DOWN);
        when(resultSet.getDouble("quantile_up")).thenReturn(TestConstants.DEFAULT_QUANTILE_UP);
        when(resultSet.getDouble("quantile_ping")).thenReturn(TestConstants.DEFAULT_QUANTILE_PING);
        when(resultSet.getFloat("hour")).thenReturn(TestConstants.DEFAULT_HOUR.floatValue());
        when(resultSet.getLong("count")).thenReturn(TestConstants.DEFAULT_COUNT);

        when(resultSet.next()).thenReturn(true, true, false);
        var extractedResult = resultSetExtractor.getValue().extractData(resultSet);
        assertEquals(expectedResult, extractedResult);
    }
}