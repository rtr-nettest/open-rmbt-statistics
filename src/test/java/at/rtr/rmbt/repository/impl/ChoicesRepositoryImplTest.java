package at.rtr.rmbt.repository.impl;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.TestObjects;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ChoicesRepositoryImplTest {

    private static final String SQL_QUERY_COUNTRY_MOBILE = "WITH RECURSIVE t1(n) AS ( " +
            "SELECT MIN(t.mobile_network_id) " +
            "FROM test t  " +
            "LEFT JOIN network_type nt ON nt.uid=t.network_type " +
            "LEFT JOIN test_loopmode l ON (l.test_uuid = t.uuid) " +
            "LEFT JOIN device_map adm ON adm.codename=t.model " +
            "LEFT JOIN test_server ts ON ts.uid=t.server_id " +
            "LEFT JOIN provider prov ON provider_id = prov.uid  " +
            "LEFT JOIN provider mprov ON mobile_provider_id = mprov.uid " +
            "LEFT JOIN mccmnc2name msim ON mobile_sim_id = msim.uid  " +
            "LEFT JOIN client c ON client_id = c.uid  " +
            "LEFT JOIN test_location tl ON t.open_test_uuid = tl.open_test_uuid " +
            "WHERE country_location ILIKE ? AND t.speed_download = ? AND t.implausible = ?  " +
            "UNION SELECT (SELECT t.mobile_network_id" +
            " FROM test t  " +
            "LEFT JOIN network_type nt ON nt.uid=t.network_type " +
            "LEFT JOIN test_loopmode l ON (l.test_uuid = t.uuid) " +
            "LEFT JOIN device_map adm ON adm.codename=t.model " +
            "LEFT JOIN test_server ts ON ts.uid=t.server_id " +
            "LEFT JOIN provider prov ON provider_id = prov.uid  " +
            "LEFT JOIN provider mprov ON mobile_provider_id = mprov.uid " +
            "LEFT JOIN mccmnc2name msim ON mobile_sim_id = msim.uid  " +
            "LEFT JOIN client c ON client_id = c.uid  " +
            "LEFT JOIN test_location tl ON t.open_test_uuid = tl.open_test_uuid " +
            "WHERE t.mobile_network_id > n AND country_location ILIKE ? AND t.speed_download = ? AND t.implausible = ? " +
            " ORDER BY t.mobile_network_id LIMIT 1) " +
            "FROM t1  )" +
            "SELECT upper(msim.country) " +
            "FROM t1 " +
            "LEFT JOIN mccmnc2name msim ON msim.uid=n " +
            "WHERE NOT upper(msim.country) IS NULL " +
            "GROUP BY upper(msim.country);";

    private static final String SQL_QUERY_PROVIDER_MOBILE = "WITH RECURSIVE t1(n) AS ( " +
            "SELECT MIN(t.mobile_provider_id) " +
            "FROM test t  " +
            "LEFT JOIN network_type nt ON nt.uid=t.network_type " +
            "LEFT JOIN test_loopmode l ON (l.test_uuid = t.uuid) " +
            "LEFT JOIN device_map adm ON adm.codename=t.model " +
            "LEFT JOIN test_server ts ON ts.uid=t.server_id " +
            "LEFT JOIN provider prov ON provider_id = prov.uid  " +
            "LEFT JOIN provider mprov ON mobile_provider_id = mprov.uid " +
            "LEFT JOIN mccmnc2name msim ON mobile_sim_id = msim.uid  " +
            "LEFT JOIN client c ON client_id = c.uid  " +
            "LEFT JOIN test_location tl ON t.open_test_uuid = tl.open_test_uuid " +
            "WHERE country_location ILIKE ? AND t.speed_download = ? AND t.implausible = ?  " +
            "UNION SELECT (SELECT t.mobile_provider_id " +
            "FROM test t  " +
            "LEFT JOIN network_type nt ON nt.uid=t.network_type " +
            "LEFT JOIN test_loopmode l ON (l.test_uuid = t.uuid) " +
            "LEFT JOIN device_map adm ON adm.codename=t.model " +
            "LEFT JOIN test_server ts ON ts.uid=t.server_id " +
            "LEFT JOIN provider prov ON provider_id = prov.uid  " +
            "LEFT JOIN provider mprov ON mobile_provider_id = mprov.uid " +
            "LEFT JOIN mccmnc2name msim ON mobile_sim_id = msim.uid  " +
            "LEFT JOIN client c ON client_id = c.uid  " +
            "LEFT JOIN test_location tl ON t.open_test_uuid = tl.open_test_uuid " +
            "WHERE t.mobile_provider_id > n AND country_location ILIKE ? AND t.speed_download = ? AND t.implausible = ?  " +
            "ORDER BY t.mobile_provider_id LIMIT 1) FROM t1  )" +
            "SELECT mprov.name " +
            "FROM t1 " +
            "LEFT JOIN provider mprov ON mprov.uid=n " +
            "WHERE NOT mprov.name IS NULL " +
            "GROUP BY mprov.name;";

    private static final String SQL_QUERY_PROVIDER = "WITH RECURSIVE t1(n) AS ( " +
            "SELECT MIN(t.provider_id) " +
            "FROM test t  " +
            "LEFT JOIN network_type nt ON nt.uid=t.network_type " +
            "LEFT JOIN test_loopmode l ON (l.test_uuid = t.uuid) " +
            "LEFT JOIN device_map adm ON adm.codename=t.model " +
            "LEFT JOIN test_server ts ON ts.uid=t.server_id " +
            "LEFT JOIN provider prov ON provider_id = prov.uid  " +
            "LEFT JOIN provider mprov ON mobile_provider_id = mprov.uid " +
            "LEFT JOIN mccmnc2name msim ON mobile_sim_id = msim.uid  " +
            "LEFT JOIN client c ON client_id = c.uid  " +
            "LEFT JOIN test_location tl ON t.open_test_uuid = tl.open_test_uuid " +
            "WHERE country_location ILIKE ? AND t.speed_download = ? AND t.implausible = ?  " +
            "UNION SELECT (SELECT t.provider_id " +
            "FROM test t  " +
            "LEFT JOIN network_type nt ON nt.uid=t.network_type " +
            "LEFT JOIN test_loopmode l ON (l.test_uuid = t.uuid) " +
            "LEFT JOIN device_map adm ON adm.codename=t.model " +
            "LEFT JOIN test_server ts ON ts.uid=t.server_id " +
            "LEFT JOIN provider prov ON provider_id = prov.uid  " +
            "LEFT JOIN provider mprov ON mobile_provider_id = mprov.uid " +
            "LEFT JOIN mccmnc2name msim ON mobile_sim_id = msim.uid  " +
            "LEFT JOIN client c ON client_id = c.uid  " +
            "LEFT JOIN test_location tl ON t.open_test_uuid = tl.open_test_uuid " +
            "WHERE t.provider_id > n AND country_location ILIKE ? AND t.speed_download = ? AND t.implausible = ?  " +
            "ORDER BY t.provider_id LIMIT 1) FROM t1  )" +
            "SELECT prov.name " +
            "FROM t1 " +
            "LEFT JOIN provider prov ON prov.uid=n " +
            "WHERE NOT prov.name IS NULL " +
            "GROUP BY prov.name;";
    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ChoicesRepositoryImpl choicesRepository;

    @Captor
    private ArgumentCaptor<PreparedStatementCreator> preparedStatementCreator;
    @Captor
    private ArgumentCaptor<PreparedStatementSetter> preparedStatementSetter;
    @Captor
    private ArgumentCaptor<ResultSetExtractor<Set<String>>> resultSetExtractor;

    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    @Test
    void findCountryMobile_correctInvocation_expectedCountryMobile() throws SQLException {
        var expectedResult = Set.of(TestConstants.DEFAULT_COUNTRY_MOBILE);
        choicesRepository.findCountryMobile(TestObjects.queryParser());

        verify(jdbcTemplate).query(preparedStatementCreator.capture(), preparedStatementSetter.capture(), resultSetExtractor.capture());

        preparedStatementCreator.getValue().createPreparedStatement(connection);
        verify(connection).prepareStatement(SQL_QUERY_COUNTRY_MOBILE);

        preparedStatementSetter.getValue().setValues(preparedStatement);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString(1)).thenReturn(TestConstants.DEFAULT_COUNTRY_MOBILE);
        var extractedResult = resultSetExtractor.getValue().extractData(resultSet);
        assertEquals(expectedResult, extractedResult);
    }

    @Test
    void findProviderMobile_correctInvocation_expectedCountryMobile() throws SQLException {
        var expectedResult = Set.of(TestConstants.DEFAULT_PROVIDER_MOBILE);
        choicesRepository.findProviderMobile(TestObjects.queryParser());

        verify(jdbcTemplate).query(preparedStatementCreator.capture(), preparedStatementSetter.capture(), resultSetExtractor.capture());

        preparedStatementCreator.getValue().createPreparedStatement(connection);
        verify(connection).prepareStatement(SQL_QUERY_PROVIDER_MOBILE);

        preparedStatementSetter.getValue().setValues(preparedStatement);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString(1)).thenReturn(TestConstants.DEFAULT_PROVIDER_MOBILE);
        var extractedResult = resultSetExtractor.getValue().extractData(resultSet);
        assertEquals(expectedResult, extractedResult);
    }

    @Test
    void findProvider_correctInvocation_expectedCountryMobile() throws SQLException {
        var expectedResult = Set.of(TestConstants.DEFAULT_PROVIDER);
        choicesRepository.findProvider(TestObjects.queryParser());

        verify(jdbcTemplate).query(preparedStatementCreator.capture(), preparedStatementSetter.capture(), resultSetExtractor.capture());

        preparedStatementCreator.getValue().createPreparedStatement(connection);
        verify(connection).prepareStatement(SQL_QUERY_PROVIDER);

        preparedStatementSetter.getValue().setValues(preparedStatement);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString(1)).thenReturn(TestConstants.DEFAULT_PROVIDER);
        var extractedResult = resultSetExtractor.getValue().extractData(resultSet);
        assertEquals(expectedResult, extractedResult);
    }
}