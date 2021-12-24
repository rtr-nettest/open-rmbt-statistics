package at.rtr.rmbt.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ConvertUtils {

    public String formatOpenTestUuid(String openTestUuid) {
        String formattedOpenTestUuid = openTestUuid;
        //openTestIDs are starting with "O"
        if (openTestUuid != null && openTestUuid.startsWith("O")) {
            formattedOpenTestUuid = openTestUuid.substring(1);
        }
        return formattedOpenTestUuid;
    }
}
