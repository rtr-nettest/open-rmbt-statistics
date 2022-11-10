package at.rtr.rmbt.repository;

import at.rtr.rmbt.response.HourlyStatisticResponse;
import at.rtr.rmbt.utils.QueryParser;

import java.util.List;

public interface HourlyStatisticRepository {

    List<HourlyStatisticResponse> getHourlyStatistic(QueryParser queryParser, double quantile);
}
