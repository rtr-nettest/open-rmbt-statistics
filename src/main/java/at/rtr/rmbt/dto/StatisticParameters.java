package at.rtr.rmbt.dto;

import at.rtr.rmbt.request.StatisticRequest;
import com.google.common.base.Strings;
import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

@Getter
public class StatisticParameters implements Funnel<StatisticParameters> {

    private final String lang;
    private final float quantile;
    private final int duration; //duration in days
    private final int maxDevices;
    private final String type;
    private final String networkTypeGroup;
    private final double accuracy;
    private final String country;
    private final boolean userServerSelection;
    private final java.sql.Timestamp endDate;
    private final int province;

    public StatisticParameters(String defaultLanguage, StatisticRequest statisticRequest) {
        String _lang = defaultLanguage; //needed later for json default values, e.g. "All providers"
        float _quantile = 0.5f; // median is default quantile
        int _duration = 90;
        int _maxDevices = 100;
        String _type = "mobile";
        String _networkTypeGroup = null;
        double _accuracy = -1;
        String _country = null;
        boolean _userServerSelection = false;
        java.sql.Timestamp _endDate = null;
        int _province = -1;


        _lang = ObjectUtils.defaultIfNull(statisticRequest.getLanguage(), defaultLanguage);


        final double __quantile = ObjectUtils.defaultIfNull(statisticRequest.getQuantile(), Double.NaN);
        if (__quantile >= 0 && __quantile <= 1)
            _quantile = (float) __quantile;

        final int __months = ObjectUtils.defaultIfNull(statisticRequest.getMonths(), 0); // obsolete, old format (now in days)
        if (__months > 0)
            _duration = __months * 30;

        final int __duration = ObjectUtils.defaultIfNull(statisticRequest.getDuration(), 0);
        if (__duration > 0)
            _duration = __duration;

        final int __maxDevices = ObjectUtils.defaultIfNull(statisticRequest.getMaxDevices(), 0);
        if (__maxDevices > 0)
            _maxDevices = __maxDevices;

        final String __type = statisticRequest.getType();
        if (__type != null)
            _type = __type;

        final String __networkTypeGroup = statisticRequest.getNetworkTypeGroup();
        if (__networkTypeGroup != null && !__networkTypeGroup.equalsIgnoreCase("all"))
            _networkTypeGroup = __networkTypeGroup;


        final double __accuracy = ObjectUtils.defaultIfNull(statisticRequest.getLocationAccuracy(), -1D);
        if (__accuracy != -1)
            _accuracy = __accuracy;

        final String __country = statisticRequest.getCountry();
        if (__country != null && __country.length() == 2)
            _country = __country;

        _userServerSelection = ObjectUtils.defaultIfNull(statisticRequest.getUserServerSelection(), false);
        // It returns false if there is no such key, or if the value is not Boolean.TRUE or the String "true".

        final String __endDateString = statisticRequest.getEndDate();
        if (__endDateString != null) {
            final java.sql.Timestamp __endDate = parseSqlTimestamp(__endDateString);
            _endDate = __endDate;
        }

        final int __province = ObjectUtils.defaultIfNull(statisticRequest.getProvince(), 0);
        if (__province > 0)
            _province = __province;

        lang = _lang;
        quantile = _quantile;
        duration = _duration;
        maxDevices = _maxDevices;
        type = _type;
        networkTypeGroup = _networkTypeGroup;
        accuracy = _accuracy;
        country = _country;
        userServerSelection = _userServerSelection;
        endDate = _endDate;
        province = _province;
    }

    @Override
    public void funnel(StatisticParameters o, PrimitiveSink into) {
        into
                .putUnencodedChars(o.getClass().getCanonicalName())
                .putChar(':')
                .putUnencodedChars(Strings.nullToEmpty(o.lang))
                .putFloat(o.quantile)
                .putInt(o.duration)
                .putUnencodedChars(Strings.nullToEmpty(o.type))
                .putInt(o.maxDevices)
                .putUnencodedChars(Strings.nullToEmpty(o.networkTypeGroup))
                .putDouble(o.accuracy)
                .putUnencodedChars(Strings.nullToEmpty(o.country))
                .putBoolean(o.userServerSelection)
                .putInt((endDate == null) ? 0 : (int) endDate.getTime())
                .putInt(o.province);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((lang == null) ? 0 : lang.hashCode());
        result = prime * result + maxDevices;
        result = prime * result + duration;
        result = prime * result + ((networkTypeGroup == null) ? 0 : networkTypeGroup.hashCode());
        result = prime * result + Float.floatToIntBits(quantile);
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((accuracy == -1) ? 0 : (int) accuracy);
        result = prime * result + ((country == null) ? 0 : country.hashCode());
        result = prime * result + ((userServerSelection) ? 0 : 1);
        result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
        result = prime * result + ((province == -1) ? 0 : (int) province);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StatisticParameters other = (StatisticParameters) obj;
        if (lang == null) {
            if (other.lang != null)
                return false;
        } else if (!lang.equals(other.lang))
            return false;
        if (maxDevices != other.maxDevices)
            return false;
        if (duration != other.duration)
            return false;
        if (networkTypeGroup == null) {
            if (other.networkTypeGroup != null)
                return false;
        } else if (!networkTypeGroup.equals(other.networkTypeGroup))
            return false;
        if (Float.floatToIntBits(quantile) != Float.floatToIntBits(other.quantile))
            return false;
        if (accuracy != other.accuracy) {
            return false;
        }
        if (country != null && other.country != null && !country.equals(other.country)) {
            return false;
        }
        if (userServerSelection != other.userServerSelection) {
            return false;
        }
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    private static java.sql.Timestamp parseSqlTimestamp(final String textual_date) {
        if (textual_date == null)
            return null;
        final SimpleDateFormat date_formatter = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        // interpret at UTC time
        date_formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            java.util.Date parsed = date_formatter.parse(textual_date);
            java.sql.Timestamp sql = new java.sql.Timestamp(parsed.getTime());
            return sql;
        } catch (ParseException ex) {
            return null;
        }
    }
}
