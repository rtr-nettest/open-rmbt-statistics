package at.rtr.rmbt.dto;

import lombok.Getter;

@Getter
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
