package at.rtr.rmbt.repository;

import at.rtr.rmbt.response.histogram.BucketResponse;
import at.rtr.rmbt.utils.QueryParser;

import java.util.List;

public interface HistogramRepository {
    List<BucketResponse> getJSONForHistogram(double min, double max, String s, boolean logarithmic, QueryParser qp);
}
