package at.rtr.rmbt.repository.impl;

import at.rtr.rmbt.repository.FencesRepository;


import at.rtr.rmbt.response.FencesItemDTO;
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
public class FencesRepositoryImpl implements FencesRepository {

    private static final String SQL = "SELECT fence_id,technology_id,technology,avg_ping_ms,offset_ms,duration_ms,radius, " +
            "ST_X(geom4326) AS longitude,ST_Y(geom4326) AS latitude FROM fences WHERE open_test_uuid = ?";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<FencesItemDTO> getFences(UUID openTestUuid) {
        PreparedStatementCreator preparedStatementCreator = getPreparedStatementCreator(SQL);
        PreparedStatementSetter preparedStatementSetter = getPreparedStatementSetter(openTestUuid);
        ResultSetExtractor<List<FencesItemDTO>> resultSetExtractor = getResultSetExtractor();
        return jdbcTemplate.query(preparedStatementCreator, preparedStatementSetter, resultSetExtractor);
    }

    private ResultSetExtractor<List<FencesItemDTO>> getResultSetExtractor() {
        return new ResultSetExtractor<List<FencesItemDTO>>() {
            @Override
            public List<FencesItemDTO> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<FencesItemDTO> list = new ArrayList<FencesItemDTO>();
                while (rs.next()) {
                    FencesItemDTO FencesItemDTO = new FencesItemDTO(
                            rs.getLong("fence_id"),
                            rs.getLong("technology_id"),
                            rs.getDouble("avg_ping_ms"),
                            rs.getString("technology"),
                            rs.getLong("offset_ms"),
                            rs.getLong("duration_ms"),
                            rs.getInt("radius"),
                            rs.getDouble("longitude"),
                            rs.getDouble("latitude")
                    );
                    list.add(FencesItemDTO);
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
