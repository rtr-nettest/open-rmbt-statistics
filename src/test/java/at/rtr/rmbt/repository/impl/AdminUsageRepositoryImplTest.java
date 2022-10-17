package at.rtr.rmbt.repository.impl;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.TestObjects;
import at.rtr.rmbt.response.adminUsage.SumsAndValuesResponse;
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
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class AdminUsageRepositoryImplTest {

    private static final String SQL_PLATFORMS = "SELECT date_trunc('day', time) _day, " +
            "platform, count(platform) count_platform " +
            "FROM (SELECT time, COALESCE(plattform, client_name, 'null') AS platform " +
            "FROM test  WHERE status='FINISHED' AND deleted=false AND time >= ? AND time < ? ) t " +
            "GROUP BY _day, platform " +
            "HAVING count(platform) > 0 ORDER BY _day ASC";
    private static final String SQL_PLATFORMS_LOOP_MODE = "SELECT date_trunc('day', time) _day, " +
            "platform, " +
            "count(platform) count_platform " +
            "FROM (SELECT time, COALESCE(plattform, client_name, 'null') AS platform " +
            "FROM test " +
            "INNER JOIN test_loopmode ON test.uuid = test_loopmode.test_uuid  " +
            "WHERE status='FINISHED' " +
            "AND deleted=false " +
            "AND time >= ? AND time < ? ) t " +
            "GROUP BY _day, platform HAVING count(platform) > 0 " +
            "ORDER BY _day ASC";
    private static final String SQL_CLASSIC_USAGE = "SELECT date_trunc('day', time) _day, " +
            "count(uid) count_tests, " +
            "sum(case when status='FINISHED' then 1 else 0 end) count_finished, " +
            "sum(case when status='ABORTED' then 1 else 0 end) count_aborted, " +
            "count(DISTINCT client_id) count_clients, " +
            "count(DISTINCT client_public_ip) count_ips " +
            "FROM test WHERE deleted=false AND time >= ? AND time < ? GROUP BY _day ORDER BY _day ASC";
    private static final String SQL_VERSIONS = "SELECT date_trunc('day', time) _day, " +
            "COALESCE(client_software_version,'null') \"version\", " +
            "count(client_software_version) count_version " +
            "FROM test " +
            "WHERE status='FINISHED' " +
            "AND deleted=false AND time >= ? " +
            "AND time < ? " +
            "AND plattform = ? " +
            "GROUP BY _day, client_software_version  " +
            "HAVING count(client_software_version) > 0  " +
            "ORDER BY _day ASC";
    private static final String SQL_NETWORK_GROUP_NAME = "SELECT date_trunc('day', time) _day, " +
            "COALESCE(network_group_name,'null') \"version\", " +
            "count(network_group_name) count_group_name " +
            "FROM test " +
            "WHERE status='FINISHED' " +
            "AND deleted=false " +
            "AND time >= ? " +
            "AND time < ? " +
            "GROUP BY _day, network_group_name  " +
            "HAVING count(network_group_name) > 0  " +
            "ORDER BY _day ASC";
    private static final String SQL_NETWORK_GROUP_TYPE = "SELECT date_trunc('day', time) _day, " +
            "COALESCE(network_group_type,'null') \"version\", " +
            "count(network_group_type) count_group_type " +
            "FROM test " +
            "WHERE status='FINISHED' " +
            "AND deleted=false " +
            "AND time >= ? " +
            "AND time < ? " +
            "GROUP BY _day, network_group_type  " +
            "HAVING count(network_group_type) > 0  " +
            "ORDER BY _day ASC";
    private static final String SQL_PLATFORMS_QOS = "SELECT   date_trunc('day', time) _day, count(*) count_tests, " +
            "plattform platform, " +
            "count(plattform) count_platform " +
            "FROM qos_test_result   " +
            "INNER JOIN test on qos_test_result.test_uid = test.uid " +
            "WHERE   test.status='FINISHED' " +
            "AND test.deleted=false   " +
            "AND time >= ? " +
            "AND time < ? " +
            "GROUP BY _day, plattform " +
            "HAVING count(plattform) > 0 " +
            "ORDER BY _day ASC ";
    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private AdminUsageRepositoryImpl adminUsageRepository;

    @Captor
    private ArgumentCaptor<PreparedStatementCreator> preparedStatementCreator;
    @Captor
    private ArgumentCaptor<PreparedStatementSetter> preparedStatementSetter;
    @Captor
    private ArgumentCaptor<ResultSetExtractor<SumsAndValuesResponse>> resultSetExtractor;

    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    private final Calendar CALENDAR_BEGIN = new GregorianCalendar(TestConstants.DEFAULT_YEAR, TestConstants.DEFAULT_MONTH, 1);
    private final Calendar CALENDAR_END = new GregorianCalendar(TestConstants.DEFAULT_YEAR, TestConstants.DEFAULT_MONTH, CALENDAR_BEGIN.getActualMaximum(Calendar.DAY_OF_MONTH));
    private final Timestamp BEGIN = new Timestamp(CALENDAR_BEGIN.getTimeInMillis());
    private final Timestamp END = new Timestamp(CALENDAR_END.getTimeInMillis());

    @Test
    void getPlatforms_correctInvocation_expectedSumsAndValuesResponse() throws SQLException {
        var expectedResult = TestObjects.sumsAndValuesResponsePlatformsResponse();
        adminUsageRepository.getPlatforms(BEGIN, END);

        verify(jdbcTemplate).query(preparedStatementCreator.capture(), preparedStatementSetter.capture(), resultSetExtractor.capture());

        preparedStatementCreator.getValue().createPreparedStatement(connection);
        verify(connection).prepareStatement(SQL_PLATFORMS);

        preparedStatementSetter.getValue().setValues(preparedStatement);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getDate("_day")).thenReturn(new Date(TestConstants.DEFAULT_DAY_PLATFORM));
        when(resultSet.getString("platform")).thenReturn(TestConstants.DEFAULT_FIELD_PLATFORM);
        when(resultSet.getLong("count_platform")).thenReturn(TestConstants.DEFAULT_SUM_PLATFORM);
        var extractedResult = resultSetExtractor.getValue().extractData(resultSet);
        assertEquals(expectedResult, extractedResult);
    }

    @Test
    void getLoopmodePlatforms_correctInvocation_expectedSumsAndValuesResponse() throws SQLException {
        var expectedResult = TestObjects.sumsAndValuesResponsePlatformsLoopModeResponse();
        adminUsageRepository.getLoopModePlatforms(BEGIN, END);

        verify(jdbcTemplate).query(preparedStatementCreator.capture(), preparedStatementSetter.capture(), resultSetExtractor.capture());

        preparedStatementCreator.getValue().createPreparedStatement(connection);
        verify(connection).prepareStatement(SQL_PLATFORMS_LOOP_MODE);

        preparedStatementSetter.getValue().setValues(preparedStatement);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getDate("_day")).thenReturn(new Date(TestConstants.DEFAULT_DAY_PLATFORM_LOOP_MODE));
        when(resultSet.getString("platform")).thenReturn(TestConstants.DEFAULT_FIELD_PLATFORM_LOOP_MODE);
        when(resultSet.getLong("count_platform")).thenReturn(TestConstants.DEFAULT_SUM_PLATFORM_LOOP_MODE);
        var extractedResult = resultSetExtractor.getValue().extractData(resultSet);
        assertEquals(expectedResult, extractedResult);
    }

    @Test
    void getClassicUsage_correctInvocation_expectedSumsAndValuesResponse() throws SQLException {
        var expectedResult = TestObjects.sumsAndValuesResponseUsageResponse();
        adminUsageRepository.getClassicUsage(BEGIN, END);

        verify(jdbcTemplate).query(preparedStatementCreator.capture(), preparedStatementSetter.capture(), resultSetExtractor.capture());

        preparedStatementCreator.getValue().createPreparedStatement(connection);
        verify(connection).prepareStatement(SQL_CLASSIC_USAGE);

        preparedStatementSetter.getValue().setValues(preparedStatement);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getDate("_day")).thenReturn(new Date(TestConstants.DEFAULT_DAY_USAGE));
        when(resultSet.getLong("count_tests")).thenReturn(TestConstants.DEFAULT_SUM_USAGE_TESTS);
        when(resultSet.getLong("count_finished")).thenReturn(TestConstants.DEFAULT_SUM_USAGE_FINISHED);
        when(resultSet.getLong("count_aborted")).thenReturn(TestConstants.DEFAULT_SUM_USAGE_ABORTED);
        when(resultSet.getLong("count_clients")).thenReturn(TestConstants.DEFAULT_SUM_USAGE_CLIENTS);
        when(resultSet.getLong("count_ips")).thenReturn(TestConstants.DEFAULT_SUM_USAGE_IPS);
        var extractedResult = resultSetExtractor.getValue().extractData(resultSet);

        assertEquals(expectedResult, extractedResult);
    }

    @Test
    void getVersions_Ios_expectedSumsAndValuesResponse() throws SQLException {
        var expectedResult = TestObjects.sumsAndValuesResponseVersionsIosResponse();
        adminUsageRepository.getVersions("iOS", BEGIN, END);

        verify(jdbcTemplate).query(preparedStatementCreator.capture(), preparedStatementSetter.capture(), resultSetExtractor.capture());

        preparedStatementCreator.getValue().createPreparedStatement(connection);
        verify(connection).prepareStatement(SQL_VERSIONS);

        preparedStatementSetter.getValue().setValues(preparedStatement);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getDate("_day")).thenReturn(new Date(TestConstants.DEFAULT_DAY_VERSIONS_IOS));
        when(resultSet.getString("version")).thenReturn(TestConstants.DEFAULT_FIELD_VERSIONS_IOS);
        when(resultSet.getLong("count_version")).thenReturn(TestConstants.DEFAULT_SUM_VERSIONS_IOS);
        var extractedResult = resultSetExtractor.getValue().extractData(resultSet);
        assertEquals(expectedResult, extractedResult);
    }

    @Test
    void getVersions_Android_expectedSumsAndValuesResponse() throws SQLException {
        var expectedResult = TestObjects.sumsAndValuesResponseVersionsAndroidResponse();
        adminUsageRepository.getVersions("Android", BEGIN, END);

        verify(jdbcTemplate).query(preparedStatementCreator.capture(), preparedStatementSetter.capture(), resultSetExtractor.capture());

        preparedStatementCreator.getValue().createPreparedStatement(connection);
        verify(connection).prepareStatement(SQL_VERSIONS);

        preparedStatementSetter.getValue().setValues(preparedStatement);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getDate("_day")).thenReturn(new Date(TestConstants.DEFAULT_DAY_VERSIONS_ANDROID));
        when(resultSet.getString("version")).thenReturn(TestConstants.DEFAULT_FIELD_VERSIONS_ANDROID);
        when(resultSet.getLong("count_version")).thenReturn(TestConstants.DEFAULT_SUM_VERSIONS_ANDROID);
        var extractedResult = resultSetExtractor.getValue().extractData(resultSet);
        assertEquals(expectedResult, extractedResult);
    }

    @Test
    void getVersions_Applet_expectedSumsAndValuesResponse() throws SQLException {
        var expectedResult = TestObjects.sumsAndValuesResponseVersionsAppletResponse();
        adminUsageRepository.getVersions("Applet", BEGIN, END);

        verify(jdbcTemplate).query(preparedStatementCreator.capture(), preparedStatementSetter.capture(), resultSetExtractor.capture());

        preparedStatementCreator.getValue().createPreparedStatement(connection);
        verify(connection).prepareStatement(SQL_VERSIONS);

        preparedStatementSetter.getValue().setValues(preparedStatement);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getDate("_day")).thenReturn(new Date(TestConstants.DEFAULT_DAY_VERSIONS_APPLET));
        when(resultSet.getString("version")).thenReturn(TestConstants.DEFAULT_FIELD_VERSIONS_APPLET);
        when(resultSet.getLong("count_version")).thenReturn(TestConstants.DEFAULT_SUM_VERSIONS_APPLET);
        var extractedResult = resultSetExtractor.getValue().extractData(resultSet);
        assertEquals(expectedResult, extractedResult);
    }

    @Test
    void getNetworkGroupName_correctInvocation_expectedSumsAndValuesResponse() throws SQLException {
        var expectedResult = TestObjects.sumsAndValuesResponseNetworkGroupNamesResponse();
        adminUsageRepository.getNetworkGroupName(BEGIN, END);

        verify(jdbcTemplate).query(preparedStatementCreator.capture(), preparedStatementSetter.capture(), resultSetExtractor.capture());

        preparedStatementCreator.getValue().createPreparedStatement(connection);
        verify(connection).prepareStatement(SQL_NETWORK_GROUP_NAME);

        preparedStatementSetter.getValue().setValues(preparedStatement);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getDate("_day")).thenReturn(new Date(TestConstants.DEFAULT_DAY_NETWORK_GROUP_NAMES));
        when(resultSet.getString("version")).thenReturn(TestConstants.DEFAULT_FIELD_NETWORK_GROUP_NAMES);
        when(resultSet.getLong("count_group_name")).thenReturn(TestConstants.DEFAULT_SUM_NETWORK_GROUP_NAMES);
        var extractedResult = resultSetExtractor.getValue().extractData(resultSet);
        assertEquals(expectedResult, extractedResult);
    }

    @Test
    void getNetworkGroupType_correctInvocation_expectedSumsAndValuesResponse() throws SQLException {
        var expectedResult = TestObjects.sumsAndValuesResponseNetworkGroupTypesResponse();
        adminUsageRepository.getNetworkGroupType(BEGIN, END);

        verify(jdbcTemplate).query(preparedStatementCreator.capture(), preparedStatementSetter.capture(), resultSetExtractor.capture());

        preparedStatementCreator.getValue().createPreparedStatement(connection);
        verify(connection).prepareStatement(SQL_NETWORK_GROUP_TYPE);

        preparedStatementSetter.getValue().setValues(preparedStatement);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getDate("_day")).thenReturn(new Date(TestConstants.DEFAULT_DAY_NETWORK_GROUP_TYPES));
        when(resultSet.getString("version")).thenReturn(TestConstants.DEFAULT_FIELD_NETWORK_GROUP_TYPES);
        when(resultSet.getLong("count_group_type")).thenReturn(TestConstants.DEFAULT_SUM_NETWORK_GROUP_TYPES);
        var extractedResult = resultSetExtractor.getValue().extractData(resultSet);
        assertEquals(expectedResult, extractedResult);
    }

    @Test
    void getQoSUsage_correctInvocation_expectedSumsAndValuesResponse() throws SQLException {
        var expectedResult = TestObjects.sumsAndValuesResponsePlatformsQosResponse();
        adminUsageRepository.getQoSUsage(BEGIN, END);

        verify(jdbcTemplate).query(preparedStatementCreator.capture(), preparedStatementSetter.capture(), resultSetExtractor.capture());

        preparedStatementCreator.getValue().createPreparedStatement(connection);
        verify(connection).prepareStatement(SQL_PLATFORMS_QOS);

        preparedStatementSetter.getValue().setValues(preparedStatement);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getDate("_day")).thenReturn(new Date(TestConstants.DEFAULT_DAY_PLATFORMS_QOS));
        when(resultSet.getString("platform")).thenReturn(TestConstants.DEFAULT_FIELD_PLATFORMS_QOS);
        when(resultSet.getLong("count_platform")).thenReturn(TestConstants.DEFAULT_SUM_PLATFORMS_QOS);
        var extractedResult = resultSetExtractor.getValue().extractData(resultSet);
        assertEquals(expectedResult, extractedResult);
    }
}