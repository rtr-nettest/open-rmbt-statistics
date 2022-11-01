package at.rtr.rmbt.repository.impl;

import at.rtr.rmbt.repository.HistogramRepository;
import at.rtr.rmbt.response.histogram.BucketResponse;
import at.rtr.rmbt.utils.QueryParser;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
@AllArgsConstructor
public class HistogramRepositoryImpl implements HistogramRepository {

    private final JdbcTemplate jdbcTemplate;
    private final static int HISTOGRAMCLASSESLOG = 12;
    private final static int HISTOGRAMCLASSES = 10;
    private final static int FINEMULTIPLIER = 10;

    @Override
    public List<BucketResponse> getJSONForHistogram(double min, double max, String field, boolean logarithmic, QueryParser qp) {
        int histogramClasses = (logarithmic) ? HISTOGRAMCLASSESLOG : HISTOGRAMCLASSES;
        histogramClasses *= FINEMULTIPLIER;

        //Get min and max steps
        double difference = max - min;
        int digits = (int) Math.floor(Math.log10(difference));
        int roundTo = Math.max(0, (int) -Math.floor(Math.log10(difference / histogramClasses)));
        long upperBound = new BigDecimal(max).setScale(-digits, BigDecimal.ROUND_CEILING).longValue();
        long lowerBound = new BigDecimal(min).setScale(-digits, BigDecimal.ROUND_FLOOR).longValue();
        double step = ((double) (upperBound - lowerBound)) / ((double) histogramClasses);
        PreparedStatementCreator preparedStatementCreator = getPreparedStatementCreator(field, histogramClasses, upperBound, lowerBound, qp);
        PreparedStatementSetter preparedStatementSetter = getPreparedStatementSetter(qp);
        ResultSetExtractor<List<BucketResponse>> resultSetExtractor = getResultSetExtractor(lowerBound, step, logarithmic, roundTo, histogramClasses);
        return jdbcTemplate.query(preparedStatementCreator, preparedStatementSetter, resultSetExtractor);

    }

    private PreparedStatementCreator getPreparedStatementCreator(String field,
                                                                 int histogramClasses,
                                                                 long upperBound,
                                                                 long lowerBound,
                                                                 QueryParser queryParser) {
        return new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                String sql = "select "
                        + " width_bucket(" + field + "," + lowerBound + "," + upperBound + "," + histogramClasses + ") bucket, "
                        + " count(*) cnt "
                        + " from test t "
                        + queryParser.getJoins()
                        + " where " + field + " > 0 "
                        + " AND t.deleted = false"
                        + " AND status = 'FINISHED' " + queryParser.getWhereClause("AND")
                        + " group by bucket " + "order by bucket asc;";
                return con.prepareStatement(sql);
            }
        };
    }

    private PreparedStatementSetter getPreparedStatementSetter(QueryParser queryParser) {
        return new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                queryParser.fillInWhereClause(ps, 1);
            }
        };
    }

    private ResultSetExtractor<List<BucketResponse>> getResultSetExtractor(long lowerBound,
                                                                           double step,
                                                                           boolean isLogarithmic,
                                                                           int roundTo,
                                                                           int histogramClasses) {
        return new ResultSetExtractor<List<BucketResponse>>() {
            @Override
            public List<BucketResponse> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<BucketResponse> buckets = new ArrayList<>();
                BucketResponse bucketObj;
                long prevCnt = 0;
                int prevBucket = 0;
                while (rs.next()) {
                    int bucket = rs.getInt("bucket");
                    long cnt = rs.getLong("cnt");

                    double current_lower_bound = lowerBound + step * (bucket - 1);
                    //logarithmic -> times 10 for kbit
                    if (isLogarithmic)
                        current_lower_bound = Math.pow(10, current_lower_bound * 4) * 10;
                    double current_upper_bound = lowerBound + (step * bucket);
                    if (isLogarithmic)
                        current_upper_bound = Math.pow(10, current_upper_bound * 4) * 10;

                    if (bucket - prevBucket > 1) {
                        //problem: bucket without values
                        //solution: respond with classes with "0" elements in them
                        int diff = bucket - prevBucket;
                        for (int i = 1; i < diff; i++) {
                            prevBucket++;
                            //jBucket = new JSONObject();
                            bucketObj = new BucketResponse();
                            double tLowerBound = lowerBound + step * (prevBucket - 1);
                            if (isLogarithmic)
                                tLowerBound = Math.pow(10, tLowerBound * 4) * 10;
                            double tUpperBound = lowerBound + (step * prevBucket);
                            if (isLogarithmic)
                                tUpperBound = Math.pow(10, tUpperBound * 4) * 10;

                            bucketObj.lowerBound = BigDecimal.valueOf(tLowerBound).setScale(roundTo, BigDecimal.ROUND_HALF_UP).doubleValue();
                            bucketObj.upperBound = BigDecimal.valueOf(tUpperBound).setScale(roundTo, BigDecimal.ROUND_HALF_UP).doubleValue();
                            bucketObj.results = 0;
                            buckets.add(bucketObj);
                        }
                    }
                    prevBucket = bucket;
                    prevCnt = cnt;

                    bucketObj = new BucketResponse();
                    if (bucket == 0) {
                        bucketObj.lowerBound = null;
                    } else {
                        //2 digits accuracy for small differences
                        bucketObj.lowerBound = BigDecimal.valueOf(current_lower_bound).setScale(roundTo, BigDecimal.ROUND_HALF_UP).doubleValue();
                    }

                    if (bucket == histogramClasses + 1) {
                        //jBucket.put("upperBound", JSONObject.NULL);
                        bucketObj.upperBound = null;
                    } else {
                        bucketObj.upperBound = BigDecimal.valueOf(current_upper_bound).setScale(roundTo, BigDecimal.ROUND_HALF_UP).doubleValue();
                    }
                    //jBucket.put("results", cnt);
                    bucketObj.results = cnt;

                    //jArray.put(jBucket);
                    buckets.add(bucketObj);
                }

                //problem: not enough buckets
                //solution: respond with classes with "0" elements
                if (buckets.size() < histogramClasses) {
                    int diff = histogramClasses - buckets.size();
                    int bucket = buckets.size();
                    for (int i = 0; i < diff; i++) {
                        bucketObj = new BucketResponse();
                        bucket++;
                        double tLowerBound = lowerBound + step * (bucket - 1);
                        if (isLogarithmic)
                            tLowerBound = Math.pow(10, tLowerBound * 4) * 10;
                        double tUpperBound = lowerBound + (step * bucket);
                        if (isLogarithmic)
                            tUpperBound = Math.pow(10, tUpperBound * 4) * 10;
                        bucketObj.lowerBound = tLowerBound;
                        bucketObj.upperBound = tUpperBound;
                        bucketObj.results = 0;

                        buckets.add(bucketObj);
                    }
                }

                return buckets;
            }
        };
    }
}
