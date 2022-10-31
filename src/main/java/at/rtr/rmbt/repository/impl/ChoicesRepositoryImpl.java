package at.rtr.rmbt.repository.impl;

import at.rtr.rmbt.repository.ChoicesRepository;
import at.rtr.rmbt.utils.QueryParser;
import lombok.RequiredArgsConstructor;
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
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

@Repository
@RequiredArgsConstructor
public class ChoicesRepositoryImpl implements ChoicesRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Set<String> findCountryMobile(QueryParser queryParser) {
        Set<String> countriesMobile = new HashSet<>();
        PreparedStatementCreator preparedStatementCreator = getPreparedStatementCreator("upper(msim.country)", "t.mobile_network_id", "mccmnc2name msim ON msim.uid", queryParser);
        PreparedStatementSetter preparedStatementSetter = getPreparedStatementSetter(queryParser);
        ResultSetExtractor<Set<String>> resultSetExtractor = getResultSetExtractor();
        try {
            countriesMobile = jdbcTemplate.query(preparedStatementCreator, preparedStatementSetter, resultSetExtractor);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return countriesMobile;
    }

    @Override
    public Set<String> findProviderMobile(QueryParser queryParser) {
        Set<String> providersMobile = new HashSet<>();
        PreparedStatementCreator preparedStatementCreator = getPreparedStatementCreator("mprov.name", "t.mobile_provider_id", "provider mprov ON mprov.uid", queryParser);
        PreparedStatementSetter preparedStatementSetter = getPreparedStatementSetter(queryParser);
        ResultSetExtractor<Set<String>> resultSetExtractor = getResultSetExtractor();
        try {
            providersMobile = jdbcTemplate.query(preparedStatementCreator, preparedStatementSetter, resultSetExtractor);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return providersMobile;
    }

    @Override
    public Set<String> findProvider(QueryParser queryParser) {
        Set<String> providers = new HashSet<>();
        PreparedStatementCreator preparedStatementCreator = getPreparedStatementCreator("prov.name", "t.provider_id", "provider prov ON prov.uid", queryParser);
        PreparedStatementSetter preparedStatementSetter = getPreparedStatementSetter(queryParser);
        ResultSetExtractor<Set<String>> resultSetExtractor = getResultSetExtractor();
        try {
            providers = jdbcTemplate.query(preparedStatementCreator, preparedStatementSetter, resultSetExtractor);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return providers;
    }

    private PreparedStatementSetter getPreparedStatementSetter(QueryParser queryParser) {
        return new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                int newIndex = queryParser.fillInWhereClause(ps, 1);
                queryParser.fillInWhereClause(ps, newIndex);
            }
        };
    }

    private PreparedStatementCreator getPreparedStatementCreator(String dbField, String dbKey, String join, QueryParser queryParser) {
        return new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                String sql = "WITH RECURSIVE t1(n) AS ( "
                        + "SELECT MIN(" + dbKey + ") FROM test t " + queryParser.getJoins() + queryParser.getWhereClause("WHERE")
                        + " UNION"
                        + " SELECT (SELECT " + dbKey + " FROM test t "
                        + queryParser.getJoins() + " WHERE " + dbKey + " > n" + queryParser.getWhereClause("AND")
                        + " ORDER BY " + dbKey + " LIMIT 1)"
                        + " FROM t1 "
                        + " )"
                        + "SELECT " + dbField + " FROM t1 LEFT JOIN " + join + "=n WHERE NOT " + dbField + " IS NULL GROUP BY " + dbField + ";";
                return con.prepareStatement(sql);
            }
        };
    }

    private ResultSetExtractor<Set<String>> getResultSetExtractor() {
        return new ResultSetExtractor<Set<String>>() {
            @Override
            public Set<String> extractData(ResultSet rs) throws SQLException, DataAccessException {
                Set<String> countries = new TreeSet<>();
                while (rs.next()) {
                    countries.add(rs.getString(1));
                }
                return countries;
            }
        };
    }
}
