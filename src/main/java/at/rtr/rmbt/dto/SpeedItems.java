package at.rtr.rmbt.dto;

import at.rtr.rmbt.utils.smoothing.Smoothable;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

public class SpeedItems {

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class SpeedItem implements Comparable<SpeedItem>, Smoothable {
        @JsonProperty("t")
        protected long time;

        @JsonProperty("b")
        protected long bytes;

        @Override
        public double getXValue() {
            return getTime();
        }

        @Override
        public double getYValue() {
            return getBytes();
        }

        @Override
        public int compareTo(SpeedItem o) {
            if (o == null)
                return -1;
            return Long.compare(time, o.time);
        }

        @Override
        public String toString() {
            return "SpeedItem [time=" + time + ", bytes=" + bytes + "]";
        }
    }

    protected Map<Integer, List<SpeedItem>> download;
    protected Map<Integer, List<SpeedItem>> upload;

    protected static Map<Integer, List<SpeedItem>> addSpeedItem(SpeedItem si, int thread, Map<Integer, List<SpeedItem>> target) {
        if (target == null)
            target = new HashMap<>();
        List<SpeedItem> speedThread = target.get(thread);
        if (speedThread == null) {
            speedThread = new ArrayList<>();
            target.put(thread, speedThread);
        }
        speedThread.add(si);
        return target;
    }

    public void addSpeedItemDownload(SpeedItem si, int thread) {
        download = addSpeedItem(si, thread, download);
    }

    public void addSpeedItemUpload(SpeedItem si, int thread) {
        upload = addSpeedItem(si, thread, upload);
    }

    public Map<Integer, List<SpeedItem>> getDownload() {
        return download;
    }

    public Map<Integer, List<SpeedItem>> getUpload() {
        return upload;
    }

    protected static void sortItems(Map<Integer, List<SpeedItem>> items) {
        if (items == null)
            return;
        for (List<SpeedItem> list : items.values())
            Collections.sort(list);
    }

    private static void reduceToUniqueTimestamps(Map<Integer, List<SpeedItem>> items) {
        if (items == null) {
            return;
        }
        for (List<SpeedItem> list : items.values()) {
            ListIterator<SpeedItem> iterator = list.listIterator();

            while (iterator.hasNext()) {
                SpeedItem item = iterator.next();
                //if the same items has same time stamp, remove from list, use only max
                if (iterator.hasNext()) {
                    SpeedItem nextItem = list.get(iterator.nextIndex());
                    if (nextItem.getTime() == item.getTime()) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    public void sortItems() {
        sortItems(download);
        sortItems(upload);
    }

    public List<SpeedItem> getAccumulatedSpeedItemsDownload() {
        return getAccumulatedSpeedItems(download);
    }

    public List<SpeedItem> getAccumulatedSpeedItemsUpload() {
        return getAccumulatedSpeedItems(upload);
    }

    public Map<String, Map<Integer, List<SpeedItem>>> getRawJSONData() {
        HashMap<String, Map<Integer, List<SpeedItem>>> ret = new HashMap<>();
        sortItems();

        //download
        ret.put("download", download);

        //upload
        ret.put("upload", upload);

        return ret;
    }


    protected static List<SpeedItem> getAccumulatedSpeedItems(Map<Integer, List<SpeedItem>> items) {
        sortItems(items);//sort items by timestamp
        reduceToUniqueTimestamps(items);

        if (items == null) {
            return new ArrayList<>();
        }

        int numItems = 0;
        for (List<SpeedItem> speedItems : items.values()) //total number of values
            numItems += speedItems.size();

        final long times[] = new long[numItems];//all timestamps, in each thread, sorted by time ascending

        int i = 0;
        for (List<SpeedItem> speedItems : items.values()) {
            for (SpeedItem item : speedItems)
                times[i++] = item.time;
            if (i == times.length)
                break;
        }

        numItems = i;
        Arrays.sort(times); //sort timestamps in total

        final long bytes[] = new long[times.length];
        for (Map.Entry<Integer, List<SpeedItem>> entry : items.entrySet())//iterate over each thread
        {
            i = 0;
            long lastTime = 0;
            long lastBytes = 0;
            for (SpeedItem si : entry.getValue()) {
                while (si.time > times[i]) // average times we don't have in a specific thread --> avg bytes transferred
                {
                    bytes[i] += Math.round((double) ((times[i] - lastTime) * si.bytes + (si.time - times[i]) * lastBytes) / (si.time - lastTime));
                    i++;
                }
                if (si.time == times[i])
                    bytes[i++] += si.bytes;
                lastTime = si.time;
                lastBytes = si.bytes;
            }
            while (i < numItems)
                bytes[i++] += lastBytes; // assume no transfer after last entry; might not be the case, but assuming otherwise could be worse
        }

        final List<SpeedItem> result = new ArrayList<>();
        for (int j = 0; j < numItems; j++)
            result.add(new SpeedItem(times[j], bytes[j]));

        return result;
    }
}
