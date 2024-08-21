package at.rtr.rmbt.repository.impl;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.response.SignalGraphItemDTO;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class RadioSignalRepositoryImplTest {

    private static final String SQL_SIGNALS = "SELECT radio_cell.open_test_uuid, radio_cell.mnc, radio_cell.mcc, radio_cell.location_id, radio_cell.area_code, " +
            "radio_cell.primary_scrambling_code, radio_cell.channel_number, " +
            "nt.name network_type, technology cat_technology, signal_strength, lte_rsrp, lte_rsrq, lte_rssnr, signal_strength wifi_rssi, timing_advance, time " +
            "FROM radio_cell " +
            "JOIN radio_signal ON radio_signal.cell_uuid = radio_cell.uuid " +
            "JOIN network_type nt ON nt.uid = network_type_id " +
            "WHERE radio_signal.open_test_uuid = ? " +
            "AND (radio_cell.active = TRUE or radio_cell.cell_state = 'secondary') AND (radio_cell.primary_data_subscription = 'true' OR radio_cell.primary_data_subscription IS NULL) " +
            "  ORDER BY radio_signal.time;";

    private static final String SQL_SIGNALS_LEGACY = "SELECT test_id, nt.name network_type, nt.group_name cat_technology, signal_strength, lte_rsrp, lte_rsrq, wifi_rssi, time "
            + "FROM signal "
            + "JOIN network_type nt "
            + "ON nt.uid = network_type_id "
            + "WHERE open_test_uuid = ? "
            + "ORDER BY time;";

    @Mock
    private JdbcTemplate jdbcTemplate;
    @InjectMocks
    private RadioSignalRepositoryImpl radioSignalRepository;

    @Captor
    private ArgumentCaptor<PreparedStatementCreator> preparedStatementCreator;
    @Captor
    private ArgumentCaptor<PreparedStatementSetter> preparedStatementSetter;
    @Captor
    private ArgumentCaptor<ResultSetExtractor<List<SignalGraphItemDTO>>> resultSetExtractor;

    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    @Test
    void getSignals_correctInvocation_SignalGraphItemDTOs() throws SQLException {
        radioSignalRepository.getSignals(TestConstants.DEFAULT_OPEN_TEST_UUID, TestConstants.DEFAULT_CLIENT_TIME_LONG);

        verify(jdbcTemplate).query(preparedStatementCreator.capture(), preparedStatementSetter.capture(), resultSetExtractor.capture());

        preparedStatementCreator.getValue().createPreparedStatement(connection);
        verify(connection).prepareStatement(SQL_SIGNALS);

        preparedStatementSetter.getValue().setValues(preparedStatement);
        verify(preparedStatement).setObject(1, TestConstants.DEFAULT_OPEN_TEST_UUID);

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.isLast()).thenReturn(true);
        when(resultSet.getTimestamp("time")).thenReturn(TestConstants.DEFAULT_TIMESTAMP);
        when(resultSet.getString("network_type")).thenReturn(TestConstants.DEFAULT_NETWORK_TYPE);
        when(resultSet.getObject("lte_rsrp", Integer.class)).thenReturn(TestConstants.DEFAULT_LTE_RSRP);
        when(resultSet.getObject("lte_rsrq", Integer.class)).thenReturn(TestConstants.DEFAULT_LTE_RSRQ);
        when(resultSet.getObject("lte_rssnr", Integer.class)).thenReturn(TestConstants.DEFAULT_LTE_RSSNR);
        when(resultSet.getObject("signal_strength", Integer.class)).thenReturn(TestConstants.DEFAULT_SIGNAL_STRENGTH);
        when(resultSet.getString("cat_technology")).thenReturn(TestConstants.DEFAULT_CAT_TECHNOLOGY);
        when(resultSet.getObject("location_id", Long.class)).thenReturn(TestConstants.DEFAULT_LOCATION_ID);
        when(resultSet.getObject("area_code", Long.class)).thenReturn(TestConstants.DEFAULT_AREA_CODE);
        when(resultSet.getObject("primary_scrambling_code", Integer.class)).thenReturn(TestConstants.DEFAULT_PRIMARY_SCRAMBLING_CODE);
        when(resultSet.getObject("channel_number", Integer.class)).thenReturn(TestConstants.DEFAULT_CHANNEL_NUMBER);
        when(resultSet.getObject("timing_advance", Integer.class)).thenReturn(TestConstants.DEFAULT_TIMING_ADVANCE);

        var extractedResult = resultSetExtractor.getValue().extractData(resultSet);

        assertEquals(expectedSignalGraphItemDTOs(), extractedResult);

    }

    @Test
    void getSignalsLegacy_correctInvocation_SignalGraphItemDTOs() throws SQLException {
        radioSignalRepository.getSignalsLegacy(TestConstants.DEFAULT_OPEN_TEST_UUID, TestConstants.DEFAULT_CLIENT_TIME_LONG);

        verify(jdbcTemplate).query(preparedStatementCreator.capture(), preparedStatementSetter.capture(), resultSetExtractor.capture());

        preparedStatementCreator.getValue().createPreparedStatement(connection);
        verify(connection).prepareStatement(SQL_SIGNALS_LEGACY);

        preparedStatementSetter.getValue().setValues(preparedStatement);
        verify(preparedStatement).setObject(1, TestConstants.DEFAULT_OPEN_TEST_UUID);

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.isLast()).thenReturn(true);
        when(resultSet.getTimestamp("time")).thenReturn(TestConstants.DEFAULT_TIMESTAMP);
        when(resultSet.getString("network_type")).thenReturn(TestConstants.DEFAULT_NETWORK_TYPE);
        when(resultSet.getObject("lte_rsrp", Integer.class)).thenReturn(TestConstants.DEFAULT_LTE_RSRP);
        when(resultSet.getObject("lte_rsrq", Integer.class)).thenReturn(TestConstants.DEFAULT_LTE_RSRQ);
        when(resultSet.getObject("signal_strength", Integer.class)).thenReturn(TestConstants.DEFAULT_SIGNAL_STRENGTH);
        when(resultSet.getString("cat_technology")).thenReturn(TestConstants.DEFAULT_CAT_TECHNOLOGY);

        var extractedResult = resultSetExtractor.getValue().extractData(resultSet);

        assertEquals(expectedSignalGraphItemDTOsLegacy(), extractedResult);
    }

    private List<SignalGraphItemDTO> expectedSignalGraphItemDTOs() {
        long timeElapsed = TestConstants.DEFAULT_TIME_LONG - TestConstants.DEFAULT_CLIENT_TIME_LONG;
        SignalGraphItemDTO signalGraphItemDTO = new SignalGraphItemDTO(
                timeElapsed,
                TestConstants.DEFAULT_NETWORK_TYPE,
                TestConstants.DEFAULT_SIGNAL_STRENGTH,
                TestConstants.DEFAULT_LTE_RSRP,
                TestConstants.DEFAULT_LTE_RSRQ,
                TestConstants.DEFAULT_LTE_RSSNR,
                TestConstants.DEFAULT_CAT_TECHNOLOGY,
                TestConstants.DEFAULT_LOCATION_ID,
                TestConstants.DEFAULT_AREA_CODE,
                TestConstants.DEFAULT_PRIMARY_SCRAMBLING_CODE,
                TestConstants.DEFAULT_CHANNEL_NUMBER,
                TestConstants.DEFAULT_TIMING_ADVANCE
        );
        return List.of(signalGraphItemDTO);
    }

    private List<SignalGraphItemDTO> expectedSignalGraphItemDTOsLegacy() {
        long timeElapsed = TestConstants.DEFAULT_TIME_LONG - TestConstants.DEFAULT_CLIENT_TIME_LONG;
        SignalGraphItemDTO signalGraphItemDTO = new SignalGraphItemDTO(
                timeElapsed,
                TestConstants.DEFAULT_NETWORK_TYPE,
                TestConstants.DEFAULT_SIGNAL_STRENGTH,
                TestConstants.DEFAULT_LTE_RSRP,
                TestConstants.DEFAULT_LTE_RSRQ,
                TestConstants.DEFAULT_CAT_TECHNOLOGY
        );
        return List.of(signalGraphItemDTO);
    }
}