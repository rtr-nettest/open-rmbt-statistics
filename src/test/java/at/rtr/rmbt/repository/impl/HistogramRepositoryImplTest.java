package at.rtr.rmbt.repository.impl;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.TestObjects;
import at.rtr.rmbt.response.histogram.BucketResponse;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class HistogramRepositoryImplTest {

    private static final String SQL = "select  width_bucket(DEFAULT_HISTOGRAM_FIELD,0,1,120) bucket,  " +
            "count(*) cnt  " +
            "from test t  " +
            "LEFT JOIN network_type nt ON nt.uid=t.network_type " +
            "LEFT JOIN test_loopmode l ON (l.test_uuid = t.uuid) " +
            "LEFT JOIN device_map adm ON adm.codename=t.model " +
            "LEFT JOIN test_server ts ON ts.uid=t.server_id " +
            "LEFT JOIN provider prov ON provider_id = prov.uid  " +
            "LEFT JOIN provider mprov ON mobile_provider_id = mprov.uid " +
            "LEFT JOIN mccmnc2name msim ON mobile_sim_id = msim.uid  " +
            "LEFT JOIN client c ON client_id = c.uid  " +
            "LEFT JOIN test_location tl ON t.open_test_uuid = tl.open_test_uuid " +
            "where DEFAULT_HISTOGRAM_FIELD > 0  " +
            "AND t.deleted = false " +
            "AND status = 'FINISHED'  " +
            "AND country_location ILIKE ? " +
            "AND t.speed_download = ? " +
            "AND t.implausible = ?  " +
            "group by bucket " +
            "order by bucket asc;";

    @Mock
    private JdbcTemplate jdbcTemplate;
    @InjectMocks
    private HistogramRepositoryImpl histogramRepository;

    @Captor
    private ArgumentCaptor<PreparedStatementCreator> preparedStatementCreator;
    @Captor
    private ArgumentCaptor<PreparedStatementSetter> preparedStatementSetter;
    @Captor
    private ArgumentCaptor<ResultSetExtractor<List<BucketResponse>>> resultSetExtractor;

    @Mock
    private Connection connection;
    @Mock
    private ResultSet resultSet;

    @Test
    void getJSONForHistogram_correctInvocation_expectedBucketResponse() throws SQLException {
        var expectedResult = List.of(TestObjects.downloadKbitBucketResponse(), TestObjects.downloadKbitBucketResponse());
        histogramRepository.getJSONForHistogram(TestConstants.DEFAULT_HISTOGRAM_MIN,
                TestConstants.DEFAULT_HISTOGRAM_MAX,
                TestConstants.DEFAULT_HISTOGRAM_FIELD,
                TestConstants.DEFAULT_HISTOGRAM_LOGARITHMIC,
                TestObjects.queryParser());

        verify(jdbcTemplate).query(preparedStatementCreator.capture(), preparedStatementSetter.capture(), resultSetExtractor.capture());

        preparedStatementCreator.getValue().createPreparedStatement(connection);
        verify(connection).prepareStatement(SQL);


        when(resultSet.getInt("bucket")).thenReturn(TestConstants.DEFAULT_HISTOGRAM_BUCKET);
        when(resultSet.getLong("cnt")).thenReturn(TestConstants.DEFAULT_HISTOGRAM_COUNT);
        when(resultSet.next()).thenReturn(true, false);
        var extractedResult = resultSetExtractor.getValue().extractData(resultSet);
        assertEquals(120, extractedResult.size());
        assertEquals(TestConstants.DEFAULT_HISTOGRAM_COUNT, extractedResult.get(0).results);
    }
}