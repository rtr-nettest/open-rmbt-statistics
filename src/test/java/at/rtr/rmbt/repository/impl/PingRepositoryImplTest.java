package at.rtr.rmbt.repository.impl;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.response.PingGraphItemDTO;
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

@ExtendWith({SpringExtension.class})
class PingRepositoryImplTest {

    private static final String SQL_QUERY = "SELECT (value_server/1e6)::float ping_ms, time_ns/1e6 time_elapsed FROM  ping WHERE open_test_uuid = ?";

    @Mock
    private JdbcTemplate jdbcTemplate;
    @InjectMocks
    private PingRepositoryImpl pingRepository;

    @Captor
    private ArgumentCaptor<PreparedStatementCreator> preparedStatementCreator;
    @Captor
    private ArgumentCaptor<PreparedStatementSetter> preparedStatementSetter;
    @Captor
    private ArgumentCaptor<ResultSetExtractor<List<PingGraphItemDTO>>> resultSetExtractor;

    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;


    @Test
    void getPingGraph_correctInvocation_PingGraphItemDTOs() throws SQLException {
        pingRepository.getPingGraph(TestConstants.DEFAULT_OPEN_TEST_UUID);

        verify(jdbcTemplate).query(preparedStatementCreator.capture(), preparedStatementSetter.capture(), resultSetExtractor.capture());

        preparedStatementCreator.getValue().createPreparedStatement(connection);
        verify(connection).prepareStatement(SQL_QUERY);

        preparedStatementSetter.getValue().setValues(preparedStatement);
        verify(preparedStatement).setObject(1, TestConstants.DEFAULT_OPEN_TEST_UUID);

        when(resultSet.getDouble("ping_ms")).thenReturn(TestConstants.DEFAULT_PING_MS);
        when(resultSet.getLong("time_elapsed")).thenReturn(TestConstants.DEFAULT_TIME_ELAPSED);
        when(resultSet.next()).thenReturn(true, false);
        var extractedResult = resultSetExtractor.getValue().extractData(resultSet);
        assertEquals(expectedPingGraphItemDTOs(), extractedResult);
    }

    private List<PingGraphItemDTO> expectedPingGraphItemDTOs() {
        PingGraphItemDTO pingGraphItemDTO = new PingGraphItemDTO(
                TestConstants.DEFAULT_PING_MS,
                TestConstants.DEFAULT_TIME_ELAPSED
        );

        return List.of(pingGraphItemDTO);
    }
}