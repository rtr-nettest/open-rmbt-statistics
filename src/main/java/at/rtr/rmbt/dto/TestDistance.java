package at.rtr.rmbt.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class TestDistance {

    private double totalDistance;
    private final double maxAccuracy;

    public TestDistance(long totalDistance, long maxAccuracy) {
        this.totalDistance = totalDistance;
        this.maxAccuracy = maxAccuracy;
        // take accuracy into account
        if (maxAccuracy > totalDistance)
            this.totalDistance = 0;
    }
}
