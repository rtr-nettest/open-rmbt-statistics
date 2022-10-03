package at.rtr.rmbt.repository.impl;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.TestObjects;
import at.rtr.rmbt.response.coverage.CoverageDTO;
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
class CoverageRepositoryImplTest {

    private static final String SQL_QUERY = "SELECT  coalesce(vn.visible_name, cov_mno_fn.operator) as operator, " +
            "cov_mno_fn.reference, " +
            "cov_mno_fn.license, " +
            "cov_mno_fn.rfc_date last_updated, " +
            "cov_mno_fn.raster, " +
            "round(cov_mno_fn.dl_max /1000)::integer downloadKbitMax, " +
            "round(cov_mno_fn.ul_max /1000)::integer uploadKbitMax, " +
            "round(cov_mno_fn.dl_normal /1000)::integer downloadKbitNormal, " +
            "round(cov_mno_fn.ul_normal/1000)::integer uploadKbitNormal, " +
            "cov_mno_fn.technology, ST_AsGeoJSON(ST_Transform(geom,4326)) geoJson " +
            "from atraster  left join cov_mno_fn on raster=id  " +
            "left join cov_visible_name vn on vn.operator = cov_mno_fn.operator  " +
            "where cov_mno_fn.raster is not null  " +
            "AND ST_intersects((ST_Transform(ST_SetSRID(ST_MakePoint(?,?),4326),3035)),geom) " +
            "order by cov_mno_fn.dl_max desc;";

    @Mock
    private JdbcTemplate jdbcTemplate;
    @InjectMocks
    private CoverageRepositoryImpl coverageRepository;

    @Captor
    private ArgumentCaptor<PreparedStatementCreator> preparedStatementCreator;
    @Captor
    private ArgumentCaptor<PreparedStatementSetter> preparedStatementSetter;
    @Captor
    private ArgumentCaptor<ResultSetExtractor<List<CoverageDTO>>> resultSetExtractor;

    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    @Test
    void getTestDistance_correctInvocation_TestDistance() throws SQLException {
        var expectedResult = List.of(TestObjects.coverageDto(), TestObjects.coverageDto());
        coverageRepository.getCoverage(TestConstants.DEFAULT_LATITUDE, TestConstants.DEFAULT_LONGITUDE);

        verify(jdbcTemplate).query(preparedStatementCreator.capture(), preparedStatementSetter.capture(), resultSetExtractor.capture());

        preparedStatementCreator.getValue().createPreparedStatement(connection);
        verify(connection).prepareStatement(SQL_QUERY);

        preparedStatementSetter.getValue().setValues(preparedStatement);
        verify(preparedStatement).setDouble(1, TestConstants.DEFAULT_LONGITUDE);
        verify(preparedStatement).setDouble(2, TestConstants.DEFAULT_LATITUDE);

        when(resultSet.getString("operator")).thenReturn(TestConstants.DEFAULT_OPERATOR);
        when(resultSet.getString("raster")).thenReturn(TestConstants.DEFAULT_RASTER);
        when(resultSet.getObject("downloadKbitMax", Integer.class)).thenReturn(TestConstants.DEFAULT_DOWNLOAD_KBIT_MAX);
        when(resultSet.getObject("uploadKbitMax", Integer.class)).thenReturn(TestConstants.DEFAULT_UPLOAD_KBIT_MAX);
        when(resultSet.getObject("downloadKbitNormal", Integer.class)).thenReturn(TestConstants.DEFAULT_DOWNLOAD_KBIT_NORMAL);
        when(resultSet.getObject("uploadKbitNormal", Integer.class)).thenReturn(TestConstants.DEFAULT_UPLOAD_KBIT_NORMAL);
        when(resultSet.getString("technology")).thenReturn(TestConstants.DEFAULT_TECHNOLOGY);
        when(resultSet.getString("last_updated")).thenReturn(TestConstants.DEFAULT_LAST_UPDATED);
        when(resultSet.getString("geoJson")).thenReturn(TestConstants.DEFAULT_GEO_JSON_OBJECT);
        when(resultSet.next()).thenReturn(true, true, false);
        var extractedResult = resultSetExtractor.getValue().extractData(resultSet);
        assertEquals(expectedResult, extractedResult);
    }
}