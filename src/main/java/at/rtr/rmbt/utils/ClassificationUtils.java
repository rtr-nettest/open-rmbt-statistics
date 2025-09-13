package at.rtr.rmbt.utils;

import at.rtr.rmbt.dto.QoeClassificationThresholds;
import at.rtr.rmbt.enums.QoeCriteria;
import at.rtr.rmbt.response.QoeClassificationResponse;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@UtilityClass
public class ClassificationUtils {

    private static final int CLASSIFICATION_ITEMS = 4;

    public static List<QoeClassificationResponse> classify(long pingNs, long downKbps, long upKbps, List<QoeClassificationThresholds> classifiers) {
        ArrayList<QoeClassificationResponse> ret = new ArrayList<>();

        for (QoeClassificationThresholds classifier : classifiers) {
            int minClass = CLASSIFICATION_ITEMS; //start with highest class, grade down
            double minQuality = 1d;


            for (Map.Entry<QoeCriteria, Long[]> entry : classifier.getThresholds().entrySet()) {
                final long value;
                switch (entry.getKey()) {
                    case DOWN:
                        value = downKbps;
                        break;
                    case UP:
                        value = upKbps;
                        break;
                    case PING:
                    default:
                        value = pingNs;
                        break;
                }

                Long[] threshold = entry.getValue();

                final boolean inverse = threshold[0] < threshold[1];
                int assignedClass;
                double assignedQuality;
                double a1 = threshold[threshold.length - 1];
                double a3 = threshold[0];
                double c = Math.sqrt(a3 / a1);
                double a0 = a1 / c;
                double a2 = a1 * c;
                double a4 = a3 * c;

                assignedQuality = ((Math.log(value) - Math.log(a0)) / (Math.log(a4) - Math.log(a0)));

                if (!inverse) {
                    //down, up
                    assignedClass = value >= a3 ? 4 : value >= a2 ? 3 : value >= a1 ? 2 : 1;

                } else {
                    assignedClass = value <= a3 ? 4 : value <= a2 ? 3 : value <= a1 ? 2 : 1;
                }
                assignedQuality = Math.max(0d, Math.min(1, assignedQuality));

                if (assignedClass == CLASSIFICATION_ITEMS) {
                    assignedQuality = 1d;
                }

                if (assignedClass < minClass) {
                    minClass = assignedClass;
                }
                if (assignedQuality < minQuality) {
                    minQuality = assignedQuality;
                }

            }
            QoeClassificationResponse newQoeClassificationResponse = QoeClassificationResponse.builder()
                    .classification(minClass)
                    .quality(minQuality)
                    .category(classifier.getQoeCategory().getValue())
                    .build();
            ret.add(newQoeClassificationResponse);
        }

        return ret;
    }
}
