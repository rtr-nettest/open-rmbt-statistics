package at.rtr.rmbt.repository.impl;

import at.rtr.rmbt.repository.HourlyStatisticRepository;
import at.rtr.rmbt.response.HourlyStatisticResponse;
import at.rtr.rmbt.utils.QueryParser;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class HourlyStatisticRepositoryImpl implements HourlyStatisticRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<HourlyStatisticResponse> getHourlyStatistic(QueryParser queryParser, double quantile) {
        PreparedStatementCreator preparedStatementCreator = getPreparedStatementCreator(queryParser);
        PreparedStatementSetter preparedStatementSetter = getPreparedStatementSetter(queryParser, quantile);
        ResultSetExtractor<List<HourlyStatisticResponse>> resultSetExtractor = getResultSetExtractor();
        return jdbcTemplate.query(preparedStatementCreator, preparedStatementSetter, resultSetExtractor);
    }

    private PreparedStatementCreator getPreparedStatementCreator(QueryParser queryParser) {
        return new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                final String sql = "SELECT" +
                        "  count(t.open_test_uuid)," +
                        "  extract(hour from t.time AT TIME ZONE t.timezone) AS hour," +
                        "       percentile_disc(?) WITHIN GROUP (ORDER BY t.speed_download :: bigint) AS quantile_down," +
                        "       percentile_disc(?) WITHIN GROUP (ORDER BY t.speed_upload :: bigint) AS quantile_up," +
                        "       percentile_disc(?) WITHIN GROUP (ORDER BY t.ping_median :: bigint) AS quantile_ping" +
                        " FROM test t " +
                        queryParser.getJoins() +
                        " WHERE t.deleted = false" +
                        " AND status = 'FINISHED' " + queryParser.getWhereClause("AND") +
                        " GROUP BY hour;";
                return con.prepareStatement(sql);
            }
        };
    }

    private PreparedStatementSetter getPreparedStatementSetter(QueryParser queryParser, double quantile) {
        return new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setDouble(1, quantile);
                ps.setDouble(2, quantile);
                ps.setDouble(3, quantile);
                queryParser.fillInWhereClause(ps, 4);
            }
        };
    }

    private ResultSetExtractor<List<HourlyStatisticResponse>> getResultSetExtractor() {
        return new ResultSetExtractor<List<HourlyStatisticResponse>>() {
            @Override
            public List<HourlyStatisticResponse> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<HourlyStatisticResponse> hourlyStatisticResponses = new ArrayList<>();
                while (rs.next()) {
                    HourlyStatisticResponse stats = new HourlyStatisticResponse(rs.getDouble("quantile_down"),
                            rs.getDouble("quantile_up"),
                            rs.getDouble("quantile_ping"),
                            rs.getFloat("hour"),
                            rs.getLong("count"));
                    hourlyStatisticResponses.add(stats);
                }
                return hourlyStatisticResponses;
            }
        };
    }
}
