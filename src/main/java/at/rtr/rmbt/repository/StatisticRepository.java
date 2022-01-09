package at.rtr.rmbt.repository;

import org.json.JSONArray;

import java.sql.SQLException;
import java.util.Set;

public interface StatisticRepository {

    Set<String> getCountries();

    JSONArray selectProviders(String lang, boolean group, float quantile, int durationDays,
                              double accuracy,
                              String country, String type, String networkTypeGroup,
                              boolean userServerSelection,
                              java.sql.Timestamp endDate, int province, boolean ultraGreen) throws
            SQLException;

    JSONArray selectDevices(String lang, boolean group, float quantile, int durationDays, double accuracy,
                            String country, String type, String networkTypeGroup, int maxDevices, boolean userServerSelection,
                            java.sql.Timestamp endDate, int province) throws SQLException;
}
