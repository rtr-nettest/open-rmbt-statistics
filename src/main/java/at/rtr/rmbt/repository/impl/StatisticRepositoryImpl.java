package at.rtr.rmbt.repository.impl;

import at.rtr.rmbt.dto.Classification;
import at.rtr.rmbt.dto.StatisticParameters;
import at.rtr.rmbt.repository.StatisticRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.Calendar;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticRepositoryImpl implements StatisticRepository {
    private static final boolean ONLY_PINNED = true;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Set<String> getCountries() {
        String sql = "WITH RECURSIVE t(n) AS ( "
                + "SELECT MIN(mobile_network_id) FROM test"
                + " UNION"
                + " SELECT (SELECT mobile_network_id FROM test WHERE mobile_network_id > n"
                + " ORDER BY mobile_network_id LIMIT 1)"
                + " FROM t WHERE n IS NOT NULL"
                + " )"
                + "SELECT upper(mccmnc2name.country) FROM t LEFT JOIN mccmnc2name ON n=mccmnc2name.uid WHERE NOT mccmnc2name.country IS NULL GROUP BY mccmnc2name.country;";

        return new TreeSet<>(jdbcTemplate.query(sql, (resultSet, i) -> resultSet.getString(1)));
    }

    @Override
    public JSONArray selectProviders(final String lang, final boolean group, final float quantile, final int durationDays,
                                     final double accuracy,
                                     final String country, final String type, final String networkTypeGroup,
                                     final boolean userServerSelection,
                                     final java.sql.Timestamp endDate, final int province, final boolean ultraGreen) throws
            SQLException {
        final boolean useMobileProvider;
        final boolean signalMobile;
        final String where;
        final String signalColumn;
        if (type.equals("mobile")) {
            signalMobile = true;
            useMobileProvider = true;

            if (networkTypeGroup == null) {
                where = "nt.type = 'MOBILE'";
                signalColumn = null;
            } else {
                if ("2G".equalsIgnoreCase(networkTypeGroup)) {
                    where = "nt.group_name = '2G'";
                    signalColumn = "signal_strength";
                } else if ("3G".equalsIgnoreCase(networkTypeGroup)) {
                    where = "nt.group_name = '3G'";
                    signalColumn = "signal_strength";
                } else if ("4G".equalsIgnoreCase(networkTypeGroup)) {
                    where = "nt.group_name = '4G'";
                    signalColumn = "lte_rsrp";
                } else if ("5G".equalsIgnoreCase(networkTypeGroup)) {
                    where = "nt.group_name = '5G'";
                    signalColumn = "lte_rsrp";
                } else if ("mixed".equalsIgnoreCase(networkTypeGroup)) {
                    where = "nt.group_name IN ('2G/3G','2G/4G','3G/4G','2G/3G/4G'," +
                            "'2G/5G', '3G/5G', '4G/5G', '2G/3G/5G', '2G/4G/5G', '3G/4G/5G')";
                    signalColumn = null;
                } else {
                    where = "1=0";
                    signalColumn = null;
                }

            }
        } else if (type.equals("wifi")) {
            where = "nt.type='WLAN'";
            signalMobile = false;
            signalColumn = "signal_strength";
            useMobileProvider = false;
        } else if (type.equals("browser")) {
            where = "nt.type = 'LAN'";
            signalMobile = false;
            signalColumn = null;
            useMobileProvider = false;
        } else {   // invalid request
            where = "1=0";
            signalMobile = false;
            signalColumn = null;
            useMobileProvider = false;
        }
        return jdbcTemplate.query(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                return getPreparedStatementSelectProviders(con, group, accuracy, country, useMobileProvider, where, endDate, province, signalColumn, ultraGreen);
            }
        }, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                setFieldSelectProvider(ps, quantile, durationDays, accuracy, country, useMobileProvider, signalMobile, endDate, province, ultraGreen);
            }
        }, new ResultSetExtractor<JSONArray>() {
            @Override
            public JSONArray extractData(ResultSet rs) throws SQLException, DataAccessException {
                JSONArray newArray = new JSONArray();
                fillJSON(lang, rs, newArray);
                return newArray;
            }
        });
    }

    private static String getClausesFor(String dbKey, String jsonKey, boolean ultraGreen, boolean inverse) {
        String sql;
        if (!ultraGreen) {
            sql = String.format(" sum((%1$s >= ?)::int)::double precision / count(%1$s) %2$s_green," +
                    " sum((%1$s < ? and %1$s >= ?)::int)::double precision / count(%1$s) %2$s_yellow," +
                    " sum((%1$s < ?)::int)::double precision / count(%1$s) %2$s_red ", dbKey, jsonKey);
        } else {
            sql = String.format(" sum((%1$s >= ?)::int)::double precision / count(%1$s) %2$s_ultragreen," +
                    " sum((%1$s < ? and %1$s >= ?)::int)::double precision / count(%1$s) %2$s_green," +
                    " sum((%1$s < ? and %1$s >= ?)::int)::double precision / count(%1$s) %2$s_yellow," +
                    " sum((%1$s < ?)::int)::double precision / count(%1$s) %2$s_red ", dbKey, jsonKey);
        }
        if (inverse) {
            sql = sql.replace(">", "[inverse]").replace("<", ">").replace("[inverse]", "<");
        }
        return sql;
    }

    private void setFieldSelectProvider(PreparedStatement ps, float quantile, int durationDays, double accuracy, String country, boolean useMobileProvider, boolean signalMobile, Timestamp endDate, int province, boolean ultraGreen) throws SQLException {
        int i = 1;
        for (int j = 0; j < 3; j++)
            ps.setFloat(i++, quantile);
        ps.setFloat(i++, 1 - quantile); // inverse for ping

        i = setThresholds(ps, ultraGreen, i, Classification.THRESHOLD_DOWNLOAD);
        i = setThresholds(ps, ultraGreen, i, Classification.THRESHOLD_UPLOAD);
        i = setThresholds(ps, ultraGreen, i, signalMobile ? Classification.THRESHOLD_SIGNAL_MOBILE : Classification.THRESHOLD_SIGNAL_WIFI);
        i = setThresholds(ps, ultraGreen, i, Classification.THRESHOLD_PING);

        if (country != null) {
            if (useMobileProvider) {
                ps.setString(i++, country.toLowerCase()); //mccmnc2name.country
                ps.setString(i++, country.toUpperCase()); //country_location
            } else {
                ps.setString(i++, country.toUpperCase());
            }
        }

        if (endDate != null) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            ps.setTimestamp(i++, endDate, cal);
        }

        ps.setString(i++, String.format("%d days", durationDays));

        if (endDate != null) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            ps.setTimestamp(i++, endDate, cal);
        }


        //ps.setBoolean(i++, userServerSelection);

        if (province != -1) {
            ps.setInt(i++, province);
        }

        if (accuracy > 0) {
            ps.setDouble(i++, accuracy);
        }
    }

    private int setThresholds(PreparedStatement ps, boolean ultraGreen, int i, int[] thresholdDownload) throws SQLException {
        final int[] td = thresholdDownload;
        if (ultraGreen) {
            ps.setInt(i++, td[0]);
            ps.setInt(i++, td[0]);
        }
        ps.setInt(i++, td[1]);
        ps.setInt(i++, td[1]);
        ps.setInt(i++, td[2]);
        ps.setInt(i++, td[2]);
        return i;
    }

    private PreparedStatement getPreparedStatementSelectProviders(final Connection conn, boolean group, double accuracy, String country, boolean useMobileProvider, String where, Timestamp endDate, int province, String signalColumn, boolean ultraGreen) throws SQLException {
        String sql = String
                .format("SELECT" +
                                (group ? " p.name, p.shortname, " : "") +
                                " count(t.uid) count," +
                                " quantile(speed_download::bigint, ?::double precision) quantile_down," +
                                " quantile(speed_upload::bigint, ?::double precision) quantile_up," +
                                " quantile(%1$s::bigint, ?::double precision) quantile_signal," +
                                " quantile(ping_median::bigint, ?::double precision) quantile_ping," +

                                getClausesFor("speed_download", "down", ultraGreen, false) + "," +
                                getClausesFor("speed_upload", "up", ultraGreen, false) + "," +
                                getClausesFor("%1$s", "signal", ultraGreen, false) + "," +
                                getClausesFor("ping_median", "ping", ultraGreen, true) +

                                " FROM test t" +
                                " LEFT JOIN network_type nt ON nt.uid=t.network_type" +
                                (((province != -1) || (accuracy > 0)) ? (" LEFT JOIN test_location tl ON t.open_test_uuid = tl.open_test_uuid") : "") +
                                " JOIN provider p ON" +
                                (useMobileProvider ? " t.mobile_provider_id = p.uid" : " t.provider_id = p.uid") +
                                " WHERE %2$s" +
                                ((country != null && useMobileProvider) ? " AND t.network_sim_country = ?" : "") +
                                " AND t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'" +
                                " AND \"time\" > " +
                                ((endDate != null) ? (" ?::TIMESTAMP WITH TIME ZONE  ") : "NOW()") +
                                " - ?::INTERVAL " +
                                ((endDate != null) ? (" AND \"time\" <=  ?::TIMESTAMP WITH TIME ZONE ") : "") +
                                //" AND user_server_selection = ? " +
                                ((province != -1) ? (" AND tl.gkz_bev/10000 = ? ") : "") +
                                ((accuracy > 0) ? " AND tl.geo_accuracy < ?" : "") +
                                ((ONLY_PINNED) ? " AND t.pinned = true" : "") +
                                (group ? " GROUP BY p.uid" : "") +
                                " ORDER BY count DESC",
                        signalColumn,
                        where);

        if (country != null) {
            sql = String
                    .format("SELECT" +
                                    ((group && useMobileProvider) ? " p.name AS name, p.shortname AS shortname,  p.mccmnc AS sim_mcc_mnc, " : "") +
                                    ((group && !useMobileProvider) ? " public_ip_as_name AS name, public_ip_as_name AS shortname, t.public_ip_asn AS asn,  " : "") +
                                    " count(t.uid) count," +
                                    " quantile(speed_download::bigint, ?::double precision) quantile_down," +
                                    " quantile(speed_upload::bigint, ?::double precision) quantile_up," +
                                    " quantile(%1$s::bigint, ?::double precision) quantile_signal," +
                                    " quantile(ping_median::bigint, ?::double precision) quantile_ping," +

                                    getClausesFor("speed_download", "down", ultraGreen, false) + "," +
                                    getClausesFor("speed_upload", "up", ultraGreen, false) + "," +
                                    getClausesFor("%1$s", "signal", ultraGreen, false) + "," +
                                    getClausesFor("ping_median", "ping", ultraGreen, true) +

                                    " FROM test t" +
                                    " LEFT JOIN network_type nt ON nt.uid=t.network_type" +
                                    (((province != -1) || (accuracy > 0)) ? (" LEFT JOIN test_location tl ON t.open_test_uuid = tl.open_test_uuid") : "") +
                                    (useMobileProvider ? " LEFT JOIN mccmnc2name p ON p.uid = t.mobile_sim_id" : "") +
                                    " WHERE %2$s" +
                                    " AND " + (useMobileProvider ? "p.country = ? AND ((t.country_location IS NULL OR t.country_location = ?)  AND (NOT t.roaming_type = 2))" : "t.country_geoip = ? ") +
                                    " AND t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'" +
                                    " AND \"time\" > " +
                                    ((endDate != null) ? (" ?::TIMESTAMP WITH TIME ZONE  ") : "NOW()") +
                                    " - ?::INTERVAL " +
                                    ((endDate != null) ? (" AND \"time\" <=  ?::TIMESTAMP WITH TIME ZONE ") : "") +
                                    //" AND user_server_selection = ? " +
                                    ((province != -1) ? (" AND tl.gkz_bev/10000 = ? ") : "") +
                                    ((accuracy > 0) ? " AND tl.geo_accuracy < ?" : "") +
                                    ((ONLY_PINNED) ? " AND t.pinned = true" : "") +
                                    ((group && (useMobileProvider)) ? " GROUP BY p.uid, p.mccmnc" : "") +
                                    ((group && (!useMobileProvider)) ? " GROUP BY t.public_ip_as_name, t.public_ip_asn" : "") +
                                    " ORDER BY count DESC",
                            signalColumn,
                            where);
        }
        return conn.prepareStatement(sql);
    }


    @Override
    public JSONArray selectDevices(final String lang, final boolean group, final float quantile, final int durationDays, final double accuracy,
                                   final String country, String type, String networkTypeGroup, final int maxDevices, final boolean userServerSelection,
                                   final java.sql.Timestamp endDate, final int province) throws SQLException {
        final boolean useMobileProvider;
        final String where;
        if (type.equals("mobile")) {
            useMobileProvider = true;

            if (networkTypeGroup == null)
                where = "nt.type = 'MOBILE'";
            else {
                if ("2G".equalsIgnoreCase(networkTypeGroup)) {
                    where = "nt.group_name = '2G'";
                } else if ("3G".equalsIgnoreCase(networkTypeGroup)) {
                    where = "nt.group_name = '3G'";
                } else if ("4G".equalsIgnoreCase(networkTypeGroup)) {
                    where = "nt.group_name = '4G'";
                } else if ("5G".equalsIgnoreCase(networkTypeGroup)) {
                    where = "nt.group_name = '5G'";
                } else if ("mixed".equalsIgnoreCase(networkTypeGroup))
                    where = "nt.group_name IN ('2G/3G','2G/4G','3G/4G','2G/3G/4G'," +
                            "'2G/5G', '3G/5G', '4G/5G', '2G/3G/5G', '2G/4G/5G', '3G/4G/5G')";
                else
                    where = "1=0";
            }
        } else if (type.equals("wifi")) {
            where = "nt.type='WLAN'";
            useMobileProvider = false;
        } else if (type.equals("browser")) {
            where = "nt.type = 'LAN'";
            useMobileProvider = false;
        } else {   // invalid request
            where = "1=0";
            useMobileProvider = false;
        }
        return jdbcTemplate.query(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                return getPreparedStatementSelectDevices(con, group, accuracy, country, useMobileProvider, where, maxDevices, endDate, province);
            }
        }, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                setFieldsPreparedStatementSelectDevices(quantile, durationDays, accuracy, country, useMobileProvider, endDate, province, ps);
            }
        }, new ResultSetExtractor<JSONArray>() {
            @Override
            public JSONArray extractData(ResultSet rs) throws SQLException, DataAccessException {
                JSONArray newArray = new JSONArray();
                fillJSON(lang, rs, newArray);
                return newArray;
            }
        });
    }

    private static PreparedStatement setFieldsPreparedStatementSelectDevices(float quantile, int durationDays, double accuracy, String country, boolean useMobileProvider, Timestamp endDate, int province, PreparedStatement ps) throws SQLException {
        int i = 1;
        for (int j = 0; j < 2; j++)
            ps.setFloat(i++, quantile);
        ps.setFloat(i++, 1 - quantile); // inverse for ping


        if (endDate != null) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            ps.setTimestamp(i++, endDate, cal);
        }

        ps.setString(i++, String.format("%d days", durationDays));

        if (endDate != null) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            ps.setTimestamp(i++, endDate, cal);
        }

        //ps.setBoolean(i++, userServerSelection);

        if (province != -1) {
            ps.setInt(i++, province);
        }

        if (country != null) {
            if (useMobileProvider) {
                ps.setString(i++, country.toLowerCase()); //mccmnc2name.country
                ps.setString(i++, country.toUpperCase()); //country_location
            } else {
                ps.setString(i++, country.toUpperCase());
            }
        }

        if (accuracy > 0) {
            ps.setDouble(i++, accuracy);
        }

        log.info(ps.toString());
        return ps;
    }

    private static PreparedStatement getPreparedStatementSelectDevices(Connection conn, boolean group, double accuracy, String country, boolean useMobileProvider, String where, int maxDevices, Timestamp endDate, int province) throws SQLException {
        String sql = String.format("SELECT" +
                (group ? " COALESCE(adm.fullname, t.model) model," : "") +
                " count(t.uid) count," + " quantile(speed_download::bigint, ?::double precision) quantile_down," +
                " quantile(speed_upload::bigint, ?::double precision) quantile_up," +
                " quantile(ping_median::bigint, ?::double precision) quantile_ping" +
                " FROM test t" +
                " LEFT JOIN device_map adm ON adm.codename=t.model" +
                " LEFT JOIN network_type nt ON nt.uid=t.network_type" +
                (((province != -1) || (accuracy > 0)) ? (" LEFT JOIN test_location tl ON t.open_test_uuid = tl.open_test_uuid") : "") +
                " WHERE %s" +
                " AND t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'" +
                " AND \"time\" > " +
                ((endDate != null) ? (" ?::TIMESTAMP WITH TIME ZONE ") : "NOW()") +
                " - ?::INTERVAL " +
                ((endDate != null) ? (" AND \"time\" <=  ?::TIMESTAMP WITH TIME ZONE ") : "") +
                //" AND user_server_selection = ? " +
                ((province != -1) ? (" AND tl.gkz_bev/10000 = ? ") : "") +
                (useMobileProvider ? " AND t.mobile_provider_id IS NOT NULL" : "") +
                ((accuracy > 0) ? " AND tl.geo_accuracy < ?" : "") +
                ((ONLY_PINNED) ? " AND t.pinned = true" : "") +
                (group ? " GROUP BY COALESCE(adm.fullname, t.model) HAVING count(t.uid) > 10" : "") +
                " ORDER BY count DESC" +
                " LIMIT %d", where, maxDevices);
        if (country != null) {
            sql = String.format("SELECT" +
                    (group ? " COALESCE(adm.fullname, t.model) model," : "") +
                    " count(t.uid) count," + " quantile(speed_download::bigint, ?::double precision) quantile_down," +
                    " quantile(speed_upload::bigint, ?::double precision) quantile_up," +
                    " quantile(ping_median::bigint, ?::double precision) quantile_ping" +
                    " FROM test t" +
                    " LEFT JOIN device_map adm ON adm.codename=t.model" +
                    " LEFT JOIN network_type nt ON nt.uid=t.network_type" +
                    (((province != -1) || (accuracy > 0)) ? (" LEFT JOIN test_location tl ON t.open_test_uuid = tl.open_test_uuid") : "") +
                    (useMobileProvider ? " LEFT JOIN mccmnc2name p ON p.uid = t.mobile_sim_id" : "") +
                    " WHERE %s" +
                    " AND t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'" +
                    " AND \"time\" > " +
                    ((endDate != null) ? (" ?::TIMESTAMP WITH TIME ZONE ") : "NOW()") +
                    " - ?::INTERVAL" +
                    ((endDate != null) ? (" AND \"time\" <=  ?::TIMESTAMP WITH TIME ZONE ") : "") +
                    //" AND user_server_selection = ? " +
                    ((province != -1) ? (" AND tl.gkz_bev/10000 = ? ") : "") +
                    " AND " + (useMobileProvider ? "p.country = ? AND ((t.country_location IS NULL OR t.country_location = ?)  AND (NOT t.roaming_type = 2))" : "t.country_geoip = ? ") +
                    ((accuracy > 0) ? " AND tl.geo_accuracy < ?" : "") +
                    ((ONLY_PINNED) ? " AND t.pinned = true" : "") +
                    (group ? " GROUP BY COALESCE(adm.fullname, t.model) HAVING count(t.uid) > 10" : "") +
                    " ORDER BY count DESC" +
                    " LIMIT %d", where, maxDevices);
        }

        return conn.prepareStatement(sql);
    }

    private void fillJSON(final String lang, final ResultSet rs, final JSONArray providers)
            throws SQLException, JSONException {
        final ResultSetMetaData metaData = rs.getMetaData();
        final int columnCount = metaData.getColumnCount();
        while (rs.next()) {
            final JSONObject obj = new JSONObject();
            for (int j = 1; j <= columnCount; j++) {
                final String colName = metaData.getColumnName(j);
                Object data = rs.getObject(j);
                if (colName.equals("name") && data == null)
                    if (lang != null && lang.equals("de"))
                        data = "Andere Betreiber";
                    else
                        data = "Other operators";
                if (colName.equals("shortname") && data == null) {
                    if (lang != null && lang.equals("de"))
                        data = "Andere";
                    else
                        data = "Others";
                }
                obj.put(colName, data);
            }
            providers.put(obj);
        }
    }
}
