package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.repository.LocationRepository;
import at.rtr.rmbt.response.LocationGraphDTO;
import at.rtr.rmbt.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;

    @Override
    public LocationGraphDTO getLocationGraph(Long testUid, long time) {
        List<LocationGraphDTO.LocationGraphItem> allResultList = locationRepository.getLocation(testUid, time);
        LocationGraphDTO.LocationGraphItem beforeTestItem = null;

        Double lastLat = null;
        Double lastLong = null;
        double totalDistance = 0;
        Iterator<LocationGraphDTO.LocationGraphItem> iterator = allResultList.iterator();
        List<LocationGraphDTO.LocationGraphItem> resultList = new ArrayList<>();

        while (iterator.hasNext()) {
            LocationGraphDTO.LocationGraphItem item = iterator.next();
            long timeElapsed = item.getTime().getTime() - time;
            item.setTimeElapsed(timeElapsed);
            //there could be measurements taken before a test started
            //in this case, only return the last one
            if (timeElapsed < 0) {
                item.setTimeElapsed(0);
                beforeTestItem = item;
            } else {
                resultList.add(item);
            }
            //only put the point in the resulting array, if there is a significant
            //distance from the last point
            //therefore (difference in m) > (tolerance last point + tolerance new point)
            if (lastLat != null && lastLong != null) {
                double diff = distFrom(lastLat, lastLong, item.getLatitude(), item.getLongitude());
                totalDistance += diff;
            }
            lastLat = item.getLatitude();
            lastLong = item.getLongitude();
        }

        //if no item was with time > 0 - add the last one here
        if (beforeTestItem != null) {
            resultList.add(0, beforeTestItem);
        }

        LocationGraphDTO lg = new LocationGraphDTO();
        lg.setLocations(resultList);
        lg.setTotalDistance(totalDistance);

        return lg;
    }

    private static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}
