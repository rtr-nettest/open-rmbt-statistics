package at.rtr.rmbt.repository.impl;

import at.rtr.rmbt.repository.CoverageRepository;
import at.rtr.rmbt.response.coverage.CoverageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.geojson.GeoJsonObject;
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
public class CoverageRepositoryImpl implements CoverageRepository {

    private static final String SQL = "SELECT " +
            " coalesce(vn.visible_name, cov_mno_fn.operator) as operator," + //varchar
            " cov_mno_fn.reference," +  //varchar
            " cov_mno_fn.license," + //text
            " cov_mno_fn.rfc_date last_updated," + //text
            " cov_mno_fn.raster," + //varchar
            " round(cov_mno_fn.dl_max /1000)::integer downloadKbitMax," +  //bigint
            " round(cov_mno_fn.ul_max /1000)::integer uploadKbitMax," + //bigint
            " round(cov_mno_fn.dl_normal /1000)::integer downloadKbitNormal," + //bigint
            " round(cov_mno_fn.ul_normal/1000)::integer uploadKbitNormal," + //bigint
            " cov_mno_fn.technology," +
            " ST_AsGeoJSON(ST_Transform(geom,4326)) geoJson" + //varchar
            " from atraster " +
            " left join cov_mno_fn on raster=id " +
            " left join cov_visible_name vn on vn.operator = cov_mno_fn.operator " +
            " where cov_mno_fn.raster is not null " +
            " AND ST_intersects((ST_Transform(ST_SetSRID(ST_MakePoint(?,?),4326),3035)),geom)" +
            " order by cov_mno_fn.dl_max desc;";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<CoverageDTO> getCoverage(Double latitude, Double longitude) {
        PreparedStatementCreator preparedStatementCreator = getPreparedStatementCreator(SQL);
        PreparedStatementSetter preparedStatementSetter = getPreparedStatementSetter(latitude, longitude);
        ResultSetExtractor<List<CoverageDTO>> resultSetExtractor = getResultSetExtractor();
        return jdbcTemplate.query(preparedStatementCreator, preparedStatementSetter, resultSetExtractor);
    }

    private PreparedStatementSetter getPreparedStatementSetter(Double latitude, Double longitude) {
        return new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setDouble(1, longitude);
                ps.setDouble(2, latitude);
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

    private ResultSetExtractor<List<CoverageDTO>> getResultSetExtractor() {
        return new ResultSetExtractor<List<CoverageDTO>>() {
            @Override
            public List<CoverageDTO> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<CoverageDTO> coverages = new ArrayList<>();
                while (rs.next()) {
                    CoverageDTO coverageDTO = null;
                    try {
                        coverageDTO = new CoverageDTO(
                                rs.getString("operator"),
                                rs.getString("raster"),
                                rs.getObject("downloadKbitMax", Integer.class),
                                rs.getObject("uploadKbitMax", Integer.class),
                                rs.getObject("downloadKbitNormal", Integer.class),
                                rs.getObject("uploadKbitNormal", Integer.class),
                                rs.getString("technology"),
                                rs.getString("last_updated"),
                                new ObjectMapper().readValue(rs.getString("geoJson"), GeoJsonObject.class)
                        );
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    coverages.add(coverageDTO);
                }
                return coverages;
            }
        };
    }
}
