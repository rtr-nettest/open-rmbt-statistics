package at.rtr.rmbt.utils;

import lombok.experimental.UtilityClass;

import java.sql.ResultSet;
import java.sql.SQLException;

@UtilityClass
public class SqlUtils {

    public Long getLongOrNull(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getObject(columnName) != null ? resultSet.getLong(columnName) : null;
    }

    public Double getDoubleOrNull(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getObject(columnName) != null ? resultSet.getDouble(columnName) : null;
    }

    public Integer getIntegerOrNull(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getObject(columnName) != null ? resultSet.getInt(columnName) : null;
    }
}
