package at.rtr.rmbt.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class LocationGraphDTO {

    private double totalDistance;

    private List<LocationGraphItem> locations = new ArrayList<>();

    @Getter
    @Setter
    public static class LocationGraphItem {
        @JsonProperty("long")
        private Double longitude;

        @JsonProperty("lat")
        private Double latitude;

        @JsonProperty("loc_accuracy")
        private Double locAccuracy;

        @JsonProperty("time_elapsed")
        private long timeElapsed;

        @JsonIgnore
        private Date time;

        @JsonProperty(value = "bearing")
        private Double bearing;

        @JsonProperty(value = "speed")
        private Double speed;

        @JsonProperty(value = "altitude")
        private Double altitude;

        @JsonProperty("loc_src")
        private String provider;

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
