package at.rtr.rmbt.response.histogram;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Builder
@Getter
@ToString
@EqualsAndHashCode
public class HistogramResponse implements Serializable {

    @JsonProperty(value = "download_kbit")
    private final List<BucketResponse> downloadKbit;
    @JsonProperty(value = "download_kbit_fine")
    private final List<BucketResponse> downloadKbitFine;
    @JsonProperty(value = "upload_kbit")
    private final List<BucketResponse> uploadKbit;
    @JsonProperty(value = "upload_kbit_fine")
    private final List<BucketResponse> uploadKbitFine;
    @JsonProperty(value = "ping_ms")
    private final List<BucketResponse> pingMs;
    @JsonProperty(value = "ping_ms_fine")
    private final List<BucketResponse> pingMsFine;
}
