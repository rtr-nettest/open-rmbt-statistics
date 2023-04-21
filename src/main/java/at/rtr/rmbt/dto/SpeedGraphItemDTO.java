package at.rtr.rmbt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SpeedGraphItemDTO {
    protected long timeElapsedNs;
    protected long bytesTotal;


    @JsonProperty("time_elapsed")
    public long getTimeElapsed() {
        return Math.round(timeElapsedNs / 1e6);
    }

    public void setTimeElapsed(long timeElapsedNs) {
        this.timeElapsedNs = timeElapsedNs;
    }

    @JsonProperty("bytes_total")
    public long getBytesTotal() {
        return bytesTotal;
    }

    public void setBytesTotal(long bytesTotal) {
        this.bytesTotal = bytesTotal;
    }

    public static class SpeedItemThreadwise extends SpeedGraphItemDTO {
        @JsonProperty("time_elapsed_ns")
        public long getTimeElapsed() {
            return timeElapsedNs;
        }
    }
}
