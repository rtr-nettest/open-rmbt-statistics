package at.rtr.rmbt.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.sql.Date;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class LocationGraphDTO {

    private double totalDistance;

    private List<LocationGraphItem> locations = new ArrayList<>();

    @Getter
    @RequiredArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class LocationGraphItem {
        @JsonProperty("long")
        private final Double longitude;

        @JsonProperty("lat")
        private final Double latitude;

        @JsonProperty("loc_accuracy")
        private final Double locAccuracy;

        @Setter
        @JsonProperty("time_elapsed")
        private long timeElapsed;

        @JsonIgnore
        private final Timestamp time;

        @JsonProperty(value = "bearing")
        private final Double bearing;

        @JsonProperty(value = "speed")
        private final Double speed;

        @JsonProperty(value = "altitude")
        private final Double altitude;

        @JsonProperty("loc_src")
        private final String provider;

        public Double getBearing() {
            if (getProvider().equals("gps")) {
                return bearing;
            }
            return null;
        }

        public Double getSpeed() {
            if (getProvider().equals("gps")) {
                return speed;
            }
            return null;
        }
    }
}
