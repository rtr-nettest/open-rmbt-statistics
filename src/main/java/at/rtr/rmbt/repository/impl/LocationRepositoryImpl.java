package at.rtr.rmbt.repository.impl;

import at.rtr.rmbt.constant.Constants;
import at.rtr.rmbt.repository.LocationRepository;
import at.rtr.rmbt.response.LocationGraphDTO;
import at.rtr.rmbt.response.PingGraphItemDTO;
import at.rtr.rmbt.service.LocationService;
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

@Service
@RequiredArgsConstructor
public class LocationRepositoryImpl implements LocationRepository {

    private final JdbcTemplate jdbcTemplate;


    @Override
    public List<LocationGraphDTO.LocationGraphItem> getLocation(Long testUid, long time) {
        final String sql = "SELECT test_id, g.geo_lat latitude, g.geo_long longitude, g.accuracy loc_accuracy, g.bearing bearing, g.speed speed, g.provider provider, altitude, time "
                + " FROM geo_location g "
                + " WHERE g.test_id = ? and accuracy < " + Constants.RMBT_GEO_ACCURACY_DETAIL_LIMIT
                + " ORDER BY time;";
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
                        ps.setLong(1, testUid);
                    }
                },
                new ResultSetExtractor<List<LocationGraphDTO.LocationGraphItem>>() {
                    @Override
                    public List<LocationGraphDTO.LocationGraphItem> extractData(ResultSet rs) throws SQLException, DataAccessException {
                        List<LocationGraphDTO.LocationGraphItem> list = new ArrayList<LocationGraphDTO.LocationGraphItem>();
                        while (rs.next()) {
                            LocationGraphDTO.LocationGraphItem locationGraphItem = new LocationGraphDTO.LocationGraphItem();
                            locationGraphItem.setLatitude(rs.getDouble("latitude"));
                            locationGraphItem.setLongitude(rs.getDouble("longitude"));
                            locationGraphItem.setLocAccuracy(rs.getDouble("loc_accuracy"));
                            locationGraphItem.setBearing(rs.getDouble("bearing"));
                            locationGraphItem.setSpeed(rs.getDouble("speed"));
                            locationGraphItem.setProvider(rs.getString("provider"));
                            locationGraphItem.setAltitude(rs.getDouble("altitude"));
                            locationGraphItem.setTime(rs.getDate("time"));
                            list.add(locationGraphItem);
                        }
                        return list;
                    }
                }
        );
    }
}
