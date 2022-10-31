package at.rtr.rmbt.repository.impl;

import at.rtr.rmbt.repository.AdminUsageRepository;
import at.rtr.rmbt.response.adminUsage.SumsAndValuesResponse;
import at.rtr.rmbt.response.adminUsage.SumsResponse;
import at.rtr.rmbt.response.adminUsage.ValueResponse;
import at.rtr.rmbt.response.adminUsage.ValuesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AdminUsageRepositoryImpl implements AdminUsageRepository {

    private final String SQL_PLATFORMS = "SELECT date_trunc('day', time) _day, platform, count(platform) count_platform" +
            " FROM (" +
            "SELECT time, COALESCE(plattform, client_name, 'null') AS platform " +
            "FROM test " +
            " WHERE status='FINISHED' AND deleted=false AND time >= ? AND time < ? " +
            ") t" +
            " GROUP BY _day, platform" +
            " HAVING count(platform) > 0" +
            " ORDER BY _day ASC";
    private final String SQL_PLATFORMS_LOOP_MODE = "SELECT date_trunc('day', time) _day, platform, count(platform) count_platform" +
            " FROM (" +
            "SELECT time, COALESCE(plattform, client_name, 'null') AS platform " +
            "FROM test " +
            "INNER JOIN test_loopmode ON test.uuid = test_loopmode.test_uuid " +
            " WHERE status='FINISHED' AND deleted=false AND time >= ? AND time < ? " +
            ") t" +
            " GROUP BY _day, platform" +
            " HAVING count(platform) > 0" +
            " ORDER BY _day ASC";
    private final String SQL_PLATFORMS_QOS = "SELECT " +
            "  date_trunc('day', time) _day, count(*) count_tests, plattform platform, count(plattform) count_platform " +
            "FROM qos_test_result " +
            "  INNER JOIN test on qos_test_result.test_uid = test.uid " +
            "WHERE " +
            "  test.status='FINISHED' AND test.deleted=false " +
            "  AND time >= ? AND time < ? " +
            "GROUP BY _day, plattform " +
            "HAVING count(plattform) > 0 " +
            "ORDER BY _day ASC ";
    private final String SQL_CLASSIC_USAGE = "SELECT date_trunc('day', time) _day, " +
            "count(uid) count_tests, " +
            "sum(case when status='FINISHED' then 1 else 0 end) count_finished, " +
            "sum(case when status='ABORTED' then 1 else 0 end) count_aborted, " +
            "count(DISTINCT client_id) count_clients, " +
            "count(DISTINCT client_public_ip) count_ips " +
            "FROM test WHERE deleted=false " +
            "AND time >= ? " +
            "AND time < ? " +
            "GROUP BY _day " +
            "ORDER BY _day ASC";
    private final String SQL_VERSION = "SELECT date_trunc('day', time) _day, COALESCE(client_software_version,'null') \"version\", count(client_software_version) count_version" +
            " FROM test" +
            " WHERE status='FINISHED' AND deleted=false AND time >= ? AND time < ? AND plattform = ?" +
            " GROUP BY _day, client_software_version " +
            " HAVING count(client_software_version) > 0 " +
            " ORDER BY _day ASC";
    private final String SQL_NETWORK_GROUP_NAME = "SELECT date_trunc('day', time) _day, COALESCE(network_group_name,'null') \"version\", count(network_group_name) count_group_name" +
            " FROM test" +
            " WHERE status='FINISHED' AND deleted=false AND time >= ? AND time < ?" +
            " GROUP BY _day, network_group_name " +
            " HAVING count(network_group_name) > 0 " +
            " ORDER BY _day ASC";
    private final String SQL_NETWORK_GROUP_TYPE = "SELECT date_trunc('day', time) _day, COALESCE(network_group_type,'null') \"version\", count(network_group_type) count_group_type" +
            " FROM test" +
            " WHERE status='FINISHED' AND deleted=false AND time >= ? AND time < ?" +
            " GROUP BY _day, network_group_type " +
            " HAVING count(network_group_type) > 0 " +
            " ORDER BY _day ASC";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public SumsAndValuesResponse getPlatforms(Timestamp begin, Timestamp end) {
        PreparedStatementCreator preparedStatementCreator = getPreparedStatementCreator(SQL_PLATFORMS);
        PreparedStatementSetter preparedStatementSetter = getPreparedStatementSetter(begin, end);
        ResultSetExtractor<SumsAndValuesResponse> resultSetExtractor = getPlatformResultSetExtractor();
        return jdbcTemplate.query(preparedStatementCreator, preparedStatementSetter, resultSetExtractor);
    }

    @Override
    public SumsAndValuesResponse getLoopModePlatforms(Timestamp begin, Timestamp end) {
        PreparedStatementCreator preparedStatementCreator = getPreparedStatementCreator(SQL_PLATFORMS_LOOP_MODE);
        PreparedStatementSetter preparedStatementSetter = getPreparedStatementSetter(begin, end);
        ResultSetExtractor<SumsAndValuesResponse> resultSetExtractor = getPlatformResultSetExtractor();
        return jdbcTemplate.query(preparedStatementCreator, preparedStatementSetter, resultSetExtractor);
    }

    @Override
    public SumsAndValuesResponse getClassicUsage(Timestamp begin, Timestamp end) {
        PreparedStatementCreator preparedStatementCreator = getPreparedStatementCreator(SQL_CLASSIC_USAGE);
        PreparedStatementSetter preparedStatementSetter = getPreparedStatementSetter(begin, end);
        ResultSetExtractor<SumsAndValuesResponse> resultSetExtractor = getClassicUsageResultSetExtractor();
        return jdbcTemplate.query(preparedStatementCreator, preparedStatementSetter, resultSetExtractor);
    }

    @Override
    public SumsAndValuesResponse getVersions(String platform, Timestamp begin, Timestamp end) {
        PreparedStatementCreator preparedStatementCreator = getPreparedStatementCreator(SQL_VERSION);
        PreparedStatementSetter preparedStatementSetter = getVersionPreparedStatementSetter(platform, begin, end);
        ResultSetExtractor<SumsAndValuesResponse> resultSetExtractor = getVersionsResultSetExtractor();
        return jdbcTemplate.query(preparedStatementCreator, preparedStatementSetter, resultSetExtractor);
    }

    @Override
    public SumsAndValuesResponse getNetworkGroupName(Timestamp begin, Timestamp end) {
        PreparedStatementCreator preparedStatementCreator = getPreparedStatementCreator(SQL_NETWORK_GROUP_NAME);
        PreparedStatementSetter preparedStatementSetter = getPreparedStatementSetter(begin, end);
        ResultSetExtractor<SumsAndValuesResponse> resultSetExtractor = getNetworkGroupNameResultSetExtractor();
        return jdbcTemplate.query(preparedStatementCreator, preparedStatementSetter, resultSetExtractor);
    }

    @Override
    public SumsAndValuesResponse getNetworkGroupType(Timestamp begin, Timestamp end) {
        PreparedStatementCreator preparedStatementCreator = getPreparedStatementCreator(SQL_NETWORK_GROUP_TYPE);
        PreparedStatementSetter preparedStatementSetter = getPreparedStatementSetter(begin, end);
        ResultSetExtractor<SumsAndValuesResponse> resultSetExtractor = getNetworkGroupTypeResultSetExtractor();
        return jdbcTemplate.query(preparedStatementCreator, preparedStatementSetter, resultSetExtractor);
    }

    @Override
    public SumsAndValuesResponse getQoSUsage(Timestamp begin, Timestamp end) {
        PreparedStatementCreator preparedStatementCreator = getPreparedStatementCreator(SQL_PLATFORMS_QOS);
        PreparedStatementSetter preparedStatementSetter = getPreparedStatementSetter(begin, end);
        ResultSetExtractor<SumsAndValuesResponse> resultSetExtractor = getPlatformResultSetExtractor();
        return jdbcTemplate.query(preparedStatementCreator, preparedStatementSetter, resultSetExtractor);
    }

    private PreparedStatementCreator getPreparedStatementCreator(String sql) {
        return new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                return con.prepareStatement(sql);
            }
        };
    }

    private PreparedStatementSetter getPreparedStatementSetter(Timestamp begin, Timestamp end) {
        return new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setTimestamp(1, begin);
                ps.setTimestamp(2, end);
            }
        };
    }

    private PreparedStatementSetter getVersionPreparedStatementSetter(String platform, Timestamp begin, Timestamp end) {
        return new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setTimestamp(1, begin);
                ps.setTimestamp(2, end);
                ps.setString(3, platform);
            }
        };
    }

    private ResultSetExtractor<SumsAndValuesResponse> getNetworkGroupTypeResultSetExtractor() {
        return new ResultSetExtractor<SumsAndValuesResponse>() {
            @Override
            public SumsAndValuesResponse extractData(ResultSet rs) throws SQLException, DataAccessException {
                return defaultSumsAndValuesResponseExtractor(rs, "version", "count_group_type");
            }
        };
    }

    private ResultSetExtractor<SumsAndValuesResponse> getPlatformResultSetExtractor() {
        return new ResultSetExtractor<SumsAndValuesResponse>() {
            @Override
            public SumsAndValuesResponse extractData(ResultSet rs) throws SQLException, DataAccessException {
                return defaultSumsAndValuesResponseExtractor(rs, "platform", "count_platform");
            }
        };
    }

    private ResultSetExtractor<SumsAndValuesResponse> getNetworkGroupNameResultSetExtractor() {
        return new ResultSetExtractor<SumsAndValuesResponse>() {
            @Override
            public SumsAndValuesResponse extractData(ResultSet rs) throws SQLException, DataAccessException {
                return defaultSumsAndValuesResponseExtractor(rs, "version", "count_group_name");
            }
        };
    }

    private ResultSetExtractor<SumsAndValuesResponse> getVersionsResultSetExtractor() {
        return new ResultSetExtractor<SumsAndValuesResponse>() {
            @Override
            public SumsAndValuesResponse extractData(ResultSet rs) throws SQLException, DataAccessException {
                return defaultSumsAndValuesResponseExtractor(rs, "version", "count_version");
            }
        };
    }

    private ResultSetExtractor<SumsAndValuesResponse> getClassicUsageResultSetExtractor() {
        return new ResultSetExtractor<SumsAndValuesResponse>() {
            @Override
            public SumsAndValuesResponse extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<SumsResponse> sums = new ArrayList<>();
                List<ValuesResponse> values = new ArrayList();

                Map<String, Long> fieldSums = new HashMap<>();
                fieldSums.put("tests", 0L);
                fieldSums.put("finished", 0L);
                fieldSums.put("aborted", 0L);
                fieldSums.put("clients", 0L);
                fieldSums.put("ips", 0L);


                while (rs.next()) {
                    List<ValueResponse> currentEntryValues = new ArrayList<>();
                    ValuesResponse entry = new ValuesResponse(rs.getDate("_day").getTime(), currentEntryValues);

                    ValueResponse jTestsAborted = new ValueResponse("aborted", rs.getLong("count_aborted"));
                    currentEntryValues.add(jTestsAborted);

                    ValueResponse jClients = new ValueResponse("clients", rs.getLong("count_clients"));
                    currentEntryValues.add(jClients);

                    ValueResponse jTestsSuccessful = new ValueResponse("finished", rs.getLong("count_finished"));
                    currentEntryValues.add(jTestsSuccessful);

                    ValueResponse jIPs = new ValueResponse("ips", rs.getLong("count_ips"));
                    currentEntryValues.add(jIPs);

                    ValueResponse jTests = new ValueResponse("tests", rs.getLong("count_tests"));
                    currentEntryValues.add(jTests);

                    fieldSums.put("tests", fieldSums.get("tests") + rs.getLong("count_tests"));
                    fieldSums.put("finished", fieldSums.get("finished") + rs.getLong("count_finished"));
                    fieldSums.put("aborted", fieldSums.get("aborted") + rs.getLong("count_aborted"));
                    fieldSums.put("clients", fieldSums.get("clients") + rs.getLong("count_clients"));
                    fieldSums.put("ips", fieldSums.get("ips") + rs.getLong("count_ips"));

                    //get some structure in there
                    values.add(entry);
                }

                //add field sums
                for (String field : fieldSums.keySet().stream().sorted().collect(Collectors.toList())) {
                    SumsResponse obj = new SumsResponse(field, fieldSums.get(field));
                    sums.add(obj);
                }

                return new SumsAndValuesResponse(sums, values);
            }
        };
    }

    private SumsAndValuesResponse defaultSumsAndValuesResponseExtractor(ResultSet rs, String fieldColumnLabel, String countColumnLabel) throws SQLException {
        List<SumsResponse> sums = new ArrayList<>();
        List<ValuesResponse> values = new ArrayList<>();

        HashMap<String, Long> fieldSums = new HashMap<>();

        //one array-item for each day
        long currentTime = -1;

        List<ValueResponse> currentEntryValues = null;
        while (rs.next()) {
            //new item, of a new day is reached
            long newTime = rs.getDate("_day").getTime();
            if (currentTime != newTime) {
                currentTime = newTime;
                currentEntryValues = new ArrayList<>();
                ValuesResponse currentEntry = new ValuesResponse(rs.getDate("_day").getTime(), currentEntryValues);
                values.add(currentEntry);
            }

            //disable null-values
            String field = rs.getString(fieldColumnLabel);
            long count = rs.getLong(countColumnLabel);
            if (field.isEmpty()) {
                field = "empty";
            }

            //add value to sum
            if (!fieldSums.containsKey(field)) {
                fieldSums.put(field, 0L);
            }
            fieldSums.put(field, fieldSums.get(field) + count);
            ValueResponse current = new ValueResponse(field, count);
            currentEntryValues.add(current);
        }

        //add field sums
        for (String field : fieldSums.keySet()) {
            SumsResponse obj = new SumsResponse(field, fieldSums.get(field));
            sums.add(obj);
        }

        return new SumsAndValuesResponse(sums, values);
    }
}
