package at.rtr.rmbt.repository.impl;

import at.rtr.rmbt.dto.TestDistance;
import at.rtr.rmbt.repository.GeoAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GeoAnalyticsRepositoryImpl implements GeoAnalyticsRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public TestDistance getTestDistance(UUID openTestUuid) {
        final String sql = "select max(g.accuracy) max_accuracy,st_lengthSpheroid(st_transform(st_makeline(g.location " +
                "order by g.time_ns),4326),'SPHEROID[\"WGS 84\",6378137,298.257223563]') as distance " +
                "from geo_location as g  where g.open_test_uuid= ? and (g.provider='gps' or g.provider='' or g.provider is null) " +
                "group by g.open_test_uuid;";
        return jdbcTemplate.query(
                new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                        return con.prepareStatement(sql);
                    }
                },
                new PreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps) throws SQLException {
                        ps.setObject(1, openTestUuid, Types.OTHER);
                    }
                },
                new ResultSetExtractor<TestDistance>() {
                    @Override
                    public TestDistance extractData(ResultSet rs) throws SQLException, DataAccessException {
                        if (rs.next()) {
                            return new TestDistance(
                                    rs.getLong("distance"),
                                    rs.getLong("max_accuracy")
                            );
                        }
                        return new TestDistance(0L, 0L);
                    }
                }
        );
    }
}
