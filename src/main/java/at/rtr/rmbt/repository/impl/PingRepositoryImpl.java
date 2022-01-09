package at.rtr.rmbt.repository.impl;

import at.rtr.rmbt.repository.PingRepository;
import at.rtr.rmbt.response.PingGraphItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PingRepositoryImpl implements PingRepository {

    private static final String SQL = "SELECT (value_server/1e6)::float ping_ms, time_ns/1e6 time_elapsed FROM  ping WHERE open_test_uuid = ?";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<PingGraphItemDTO> getPingGraph(UUID openTestUuid) {
        PreparedStatementCreator preparedStatementCreator = getPreparedStatementCreator(SQL);
        PreparedStatementSetter preparedStatementSetter = getPreparedStatementSetter(openTestUuid);
        ResultSetExtractor<List<PingGraphItemDTO>> resultSetExtractor = getResultSetExtractor();
        return jdbcTemplate.query(preparedStatementCreator, preparedStatementSetter, resultSetExtractor);
    }

    private ResultSetExtractor<List<PingGraphItemDTO>> getResultSetExtractor() {
        return new ResultSetExtractor<List<PingGraphItemDTO>>() {
            @Override
            public List<PingGraphItemDTO> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<PingGraphItemDTO> list = new ArrayList<PingGraphItemDTO>();
                while (rs.next()) {
                    PingGraphItemDTO pingGraphItemDTO = new PingGraphItemDTO(
                            rs.getDouble("ping_ms"),
                            rs.getLong("time_elapsed"));
                    list.add(pingGraphItemDTO);
                }
                return list;
            }
        };
    }

    private PreparedStatementSetter getPreparedStatementSetter(UUID openTestUuid) {
        return new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setObject(1, openTestUuid);
            }
        };
    }

    private PreparedStatementCreator getPreparedStatementCreator(String sql) {
        return new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                return con.prepareStatement(sql);
            }
        };
    }
}
