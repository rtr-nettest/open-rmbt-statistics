package at.rtr.rmbt.repository.impl;

import at.rtr.rmbt.repository.RadioSignalRepository;
import at.rtr.rmbt.response.SignalGraphItemDTO;
import at.rtr.rmbt.utils.SqlUtils;
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
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RadioSignalRepositoryImpl implements RadioSignalRepository {

    private final JdbcTemplate jdbcTemplate;

    final int LOWER_BOUND = -1500;
    final int MAX_TIME = 60000;

    @Override
    public List<SignalGraphItemDTO> getSignals(UUID openTestUuid, long time) {
        final String sql = "SELECT radio_cell.open_test_uuid, radio_cell.mnc, radio_cell.mcc, radio_cell.location_id, radio_cell.area_code, " +
                "radio_cell.primary_scrambling_code, radio_cell.channel_number, " +
                "nt.name network_type, technology cat_technology, signal_strength, lte_rsrp, lte_rsrq, signal_strength wifi_rssi, timing_advance, time " +
                "FROM radio_cell " +
                "JOIN radio_signal ON radio_signal.cell_uuid = radio_cell.uuid " +
                "JOIN network_type nt ON nt.uid = network_type_id " +
                "WHERE radio_signal.open_test_uuid = ? " +
                "AND radio_cell.active = TRUE " +
                "  ORDER BY radio_signal.time;";
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
                new ResultSetExtractor<List<SignalGraphItemDTO>>() {
                    @Override
                    public List<SignalGraphItemDTO> extractData(ResultSet rsSignal) throws SQLException, DataAccessException {
                        boolean first = true;
                        SignalGraphItemDTO item = null;
                        List<SignalGraphItemDTO> signalList = new ArrayList<>();
                        while (rsSignal.next()) {
                            long timeElapsed = rsSignal.getTimestamp("time").getTime() - time;
                            //there could be measurements taken before a test started
                            //in this case, only return the last one
                            if (first && timeElapsed > 0 && item != null) {
                                signalList.add(item);
                                first = false;
                            }

                            //ignore measurements after a threshold of one minute
                            if (timeElapsed > MAX_TIME)
                                break;

                            Integer signalStrength = rsSignal.getObject("signal_strength", Integer.class);
                            Integer lteRsrp = rsSignal.getObject("lte_rsrp", Integer.class);
                            Integer lteRsrq = rsSignal.getObject("lte_rsrq", Integer.class);
                            if (Objects.isNull(signalStrength)) {
                                signalStrength = rsSignal.getObject("wifi_rssi", Integer.class);
                            }

                            if ((Objects.nonNull(signalStrength) && signalStrength > LOWER_BOUND)
                                    || (Objects.nonNull(lteRsrp) && lteRsrp > LOWER_BOUND)
                                    || (Objects.nonNull(lteRsrq) && lteRsrq > LOWER_BOUND)) {
                                int channelNumber = rsSignal.getInt("channel_number");
                                item = new SignalGraphItemDTO();
                                item.setTimeElapsed(Math.max(timeElapsed, 0));

                                item = new SignalGraphItemDTO(
                                        Math.max(timeElapsed, 0),
                                        rsSignal.getString("network_type"),
                                        signalStrength,
                                        lteRsrp,
                                        lteRsrq,
                                        rsSignal.getString("cat_technology"),
                                        rsSignal.getInt("location_id"),
                                        rsSignal.getInt("area_code"),
                                        rsSignal.getInt("primary_scrambling_code"),
                                        channelNumber,
                                        SqlUtils.getIntegerOrNull(rsSignal, "timing_advance"));//TODO: проверить для integer null
                            }

                            //put 5-let in the array if it is not the first one
                            if (!first || rsSignal.isLast()) {
                                if (timeElapsed < 0) {
                                    item.setTimeElapsed(1000);
                                }
                                signalList.add(item);
                            }
                        }
                        return signalList;
                    }
                }
        );
    }

    @Override
    public List<SignalGraphItemDTO> getSignalsLegacy(UUID openTestUuid, long time) {
        final String sql = "SELECT test_id, nt.name network_type, nt.group_name cat_technology, signal_strength, lte_rsrp, lte_rsrq, wifi_rssi, time "
                + "FROM signal "
                + "JOIN network_type nt "
                + "ON nt.uid = network_type_id "
                + "WHERE open_test_uuid = ? "
                + "ORDER BY time;";
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
                new ResultSetExtractor<List<SignalGraphItemDTO>>() {
                    @Override
                    public List<SignalGraphItemDTO> extractData(ResultSet rsSignal) throws SQLException, DataAccessException {
                        boolean first = true;
                        SignalGraphItemDTO item = null;
                        List<SignalGraphItemDTO> signalList = new ArrayList<>();
                        while (rsSignal.next()) {
                            long timeElapsed = rsSignal.getTimestamp("time").getTime() - time;
                            //there could be measurements taken before a test started
                            //in this case, only return the last one
                            if (first && timeElapsed > 0 && item != null) {
                                signalList.add(item);
                                first = false;
                            }

                            //ignore measurements after a threshold of one minute
                            if (timeElapsed > MAX_TIME)
                                break;


                            Integer signalStrength = rsSignal.getInt("signal_strength");
                            int lteRsrp = rsSignal.getInt("lte_rsrp");
                            int lteRsrq = rsSignal.getInt("lte_rsrq");
                            if (signalStrength == 0) {
                                signalStrength = rsSignal.getInt("wifi_rssi");
                            }

                            if ((signalStrength != 0 && signalStrength > LOWER_BOUND) || lteRsrp > LOWER_BOUND || lteRsrq > LOWER_BOUND) {
                                item = new SignalGraphItemDTO(
                                        Math.max(timeElapsed, 0),
                                        rsSignal.getString("network_type"),
                                        signalStrength,
                                        lteRsrp,
                                        lteRsrq,
                                        rsSignal.getString("cat_technology"));
                            }

                            //put 5-let in the array if it is not the first one
                            if (!first || rsSignal.isLast()) {
                                if (timeElapsed < 0) {
                                    item.setTimeElapsed(1000);
                                }
                                signalList.add(item);
                            }
                        }
                        return signalList;
                    }
                }
        );
    }
}
