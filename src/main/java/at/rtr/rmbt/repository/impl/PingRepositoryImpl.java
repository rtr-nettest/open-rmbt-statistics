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

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<PingGraphItemDTO> getPingGraph(UUID openTestUuid) {
        final String sql = "SELECT (value_server/1e6)::float ping_ms, time_ns/1e6 time_elapsed FROM  ping WHERE open_test_uuid = ?";
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
                        ps.setObject(1, openTestUuid);
                    }
                },
                new ResultSetExtractor<List<PingGraphItemDTO>>() {
                    @Override
                    public List<PingGraphItemDTO> extractData(ResultSet rs) throws SQLException, DataAccessException {
                        List<PingGraphItemDTO> list = new ArrayList<PingGraphItemDTO>();
                        while (rs.next()) {
                            PingGraphItemDTO pingGraphItemDTO = new PingGraphItemDTO();
                            pingGraphItemDTO.setPingMs(rs.getDouble("ping_ms"));
                            pingGraphItemDTO.setTimeElapsed(rs.getLong("time_elapsed"));
                            list.add(pingGraphItemDTO);
                        }
                        return list;
                    }
                }
        );
    }
}
