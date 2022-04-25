package at.rtr.rmbt;

import org.springframework.http.MediaType;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

public interface TestConstants {
    String DEFAULT_GIT_BRANCH = "DEFAULT_GIT_BRANCH";
    String DEFAULT_GIT_COMMIT_ID_DESCRIBE = "DEFAULT_GIT_COMMIT_ID_DESCRIBE";
    String DEFAULT_CONTROL_SERVER_VERSION = String.format("%s_%s", DEFAULT_GIT_BRANCH, DEFAULT_GIT_COMMIT_ID_DESCRIBE);
    String DEFAULT_SYSTEM_UUID_VALUE = "d916af3d-acb9-46e0-b84e-3f8bac20ef5d";
    String DEFAULT_APPLICATION_HOST = "DEFAULT_APPLICATION_HOST";
    String DEFAULT_OPEN_TEST_UUID_STRING = "fa39d023-6b54-4008-a67e-24f5bdb579bc";
    String DEFAULT_OPEN_TEST_UUID_STRING_WITH_PREFIX = "Ofa39d023-6b54-4008-a67e-24f5bdb579bc";
    UUID DEFAULT_OPEN_TEST_UUID = UUID.fromString(DEFAULT_OPEN_TEST_UUID_STRING);
    Long DEFAULT_TEST_DISTANCE = 5L;
    Long DEFAULT_TEST_MAX_ACCURACY = 3L;
    Long DEFAULT_TEST_UID = 51L;
    long DEFAULT_TEST_TIME = 111L;
    Double DEFAULT_LONGITUDE = 18D;
    Double DEFAULT_LONGITUDE_SECOND = 16D;
    Double DEFAULT_LONGITUDE_THIRD = 28D;
    Double DEFAULT_LONGITUDE_FOURTH = 25D;
    Double DEFAULT_LATITUDE = 31.3D;
    Double DEFAULT_LATITUDE_SECOND = 45.3D;
    Double DEFAULT_LATITUDE_THIRD = 73.3D;
    Double DEFAULT_LATITUDE_FOURTH = 26.3D;
    Double DEFAULT_LOC_ACCURACY = 13.7D;
    Double DEFAULT_LOC_ACCURACY_SECOND = 28.7D;
    Double DEFAULT_LOC_ACCURACY_THIRD = 64.7D;
    Double DEFAULT_LOC_ACCURACY_FOURTH = 35.7D;
    Date DEFAULT_TIME = new Date(1133231);
    Long DEFAULT_TIME_LONG = 164174123817L;
    Long DEFAULT_INSTANT_MILLIS = 1642350991000L;
    Double DEFAULT_BEARING = 4.3D;
    Double DEFAULT_BEARING_SECOND = 7.3D;
    Double DEFAULT_BEARING_THIRD = 42.3D;
    Double DEFAULT_BEARING_FOURTH = 16.3D;
    Double DEFAULT_SPEED = 13.54D;
    Double DEFAULT_SPEED_SECOND = 14.74D;
    Double DEFAULT_SPEED_THIRD = 24.54D;
    Double DEFAULT_SPEED_FOURTH = 33.54D;
    Double DEFAULT_ALTITUDE = 17.3D;
    Double DEFAULT_ALTITUDE_SECOND = 27.3D;
    Double DEFAULT_ALTITUDE_THIRD = 57.3D;
    Double DEFAULT_ALTITUDE_FOURTH = 77.3D;
    String DEFAULT_PROVIDER = "DEFAULT_PROVIDER";
    String DEFAULT_PROVIDER_SECOND = "DEFAULT_PROVIDER_SECOND";
    String DEFAULT_PROVIDER_THIRD = "DEFAULT_PROVIDER_THIRD";
    String DEFAULT_PROVIDER_FOURTH = "DEFAULT_PROVIDER_FOURTH";
    Integer DEFAULT_HOUR = 4;
    Integer DEFAULT_YEAR = 2021;
    Integer DEFAULT_MONTH = 7;
    String DEFAULT_OPEN_UUID = "9a837fab-2d65-4256-8fbf-0fd373207d37";
    String DEFAULT_TIME_STRING = "DEFAULT_TIME_STRING";
    String DEFAULT_CAT_TECHNOLOGY = "3G";
    String DEFAULT_LOC_SRC = "DEFAULT_LOC_SRC";
    Integer DEFAULT_GKZ = 17;
    String DEFAULT_ZIP_CODE = "DEFAULT_ZIP_CODE";
    String DEFAULT_COUNTRY_LOCATION = "DEFAULT_COUNTRY_LOCATION";
    Integer DEFAULT_DOWNLOAD_KBIT = 900;
    Integer DEFAULT_UPLOAD_KBIT = 36000;
    Double DEFAULT_PING_MS = 35.3;
    Integer DEFAULT_LTE_RSRP = -34;
    Integer DEFAULT_LTE_RSRQ = 223;
    String DEFAULT_SERVER_NAME = "DEFAULT_SERVER_NAME";
    Integer DEFAULT_TEST_DURATION = 2222;
    Integer DEFAULT_NUM_THREADS = 3;
    String DEFAULT_PLATFORM = "Android";
    String DEFAULT_MODEL = "DEFAULT_MODEL";
    String DEFAULT_CLIENT_VERSION = "DEFAULT_CLIENT_VERSION";
    String DEFAULT_NETWORK_MCC_MNC = "344-32";
    String DEFAULT_NETWORK_NAME = "DEFAULT_NETWORK_NAME";
    String DEFAULT_SIM_MCC_MNC = "DEFAULT_SIM_MCC_MNC";
    String DEFAULT_NAT_TYPE = "DEFAULT_NAT_TYPE";
    Long DEFAULT_ASN = 3L;
    String DEFAULT_IP_ANONYM = "DEFAULT_IP_ANONYM";
    Integer DEFAULT_NDT_DOWNLOAD_KBIT = 18;
    Integer DEFAULT_NDT_UPLOAD_KBIT = 21;
    Boolean DEFAULT_IMPLAUSIBLE = true;
    Integer DEFAULT_SIGNAL_STRENGTH = -94;
    Boolean DEFAULT_PINNED = true;
    Integer DEFAULT_KG_NR = 33;
    Integer DEFAULT_GKZ_SA = 88;
    Integer DEFAULT_LAND_COVER = 18;
    Integer DEFAULT_CELL_AREA_CODE = 19;
    Integer DEFAULT_CELL_LOCATION_ID = 20;
    Integer DEFAULT_CHANNEL_NUMBER = 21;
    Integer DEFAULT_RADIO_BAND = 22;
    Integer DEFAULT_SETTLEMENT_TYPE = 23;
    Integer DEFAULT_LINK_ID = 25;
    String DEFAULT_LINK_NAME = "DEFAULT_LINK_NAME";
    Integer DEFAULT_LINK_DISTANCE = 30;
    String DEFAULT_EDGE_ID = "DEFAULT_EDGE_ID";
    Integer DEFAULT_LINK_FRC = 31;
    Integer DEFAULT_DTM_LEVEL = 32;
    long DEFAULT_TIME_ELAPSED = 1330300;
    Timestamp DEFAULT_TIMESTAMP = new Timestamp(DEFAULT_TIME_LONG);
    long DEFAULT_CLIENT_TIME_LONG = 164174122517L;
    Timestamp DEFAULT_CLIENT_TIME_TIMESTAMP = new Timestamp(164174122517L);
    String DEFAULT_NETWORK_TYPE = "WLAN";
    Long DEFAULT_LOCATION_ID = 33L;
    Integer DEFAULT_AREA_CODE = 34;
    Integer DEFAULT_PRIMARY_SCRAMBLING_CODE = 35;
    Integer DEFAULT_TIMING_ADVANCE = 36;
    String DEFAULT_TEXT = "DEFAULT_TEXT";
    String DEFAULT_LANGUAGE = "en";
    Double DEFAULT_UPLOAD = 13.3;
    Double DEFAULT_DOWNLOAD = 21.76;
    double DEFAULT_TOTAL_DISTANCE = 9968456.801247576;
    Long DEFAULT_NEXT_CURSOR = 3L;
    Integer DEFAULT_UPLOAD_CLASSIFICATION = 3;
    String DEFAULT_FULL_TIME_STRING = "DEFAULT_FULL_TIME_STRING";
    Integer DEFAULT_DOWNLOAD_CLASSIFICATION = 1;
    Integer DEFAULT_PING_CLASSIFICATION = 2;
    Integer DEFAULT_SIGNAL_CLASSIFICATION = 88;
    String DEFAULT_EXPECTED_CSV_RESPONSE_ENTITY = "<200 OK OK,open_uuid,open_test_uuid,time,lat,long,download_kbit,upload_kbit,ping_ms,signal_strength,lte_rsrp,platform,provider_name,model,loc_accuracy,download_classification,upload_classification,ping_classification,signal_classification9a837fab-2d65-4256-8fbf-0fd373207d37,fa39d023-6b54-4008-a67e-24f5bdb579bc,DEFAULT_TIME_STRING,31.3,18.0,900,36000,35.3,-94,-34,Android,DEFAULT_PROVIDER,DEFAULT_MODEL,13.7,1,3,2,88,[Content-Disposition:\"attachment;filename=opentests.csv\", Content-Type:\"text/csv;charset=UTF-8\"]>";
    String DEFAULT_EXPECTED_JSON_RESPONSE_ENTITY = "<200 OK OK," +
            "{\"next_cursor\":3," +
            "\"duration_ms\":2222," +
            "\"results\":[" +
            "{\"open_uuid\":\"9a837fab-2d65-4256-8fbf-0fd373207d37\"," +
            "\"open_test_uuid\":\"fa39d023-6b54-4008-a67e-24f5bdb579bc\"," +
            "\"time\":\"DEFAULT_TIME_STRING\"," +
            "\"lat\":31.3," +
            "\"long\":18.0," +
            "\"download_kbit\":900," +
            "\"upload_kbit\":36000," +
            "\"ping_ms\":35.3," +
            "\"signal_strength\":-94," +
            "\"lte_rsrp\":-34," +
            "\"platform\":\"Android\"," +
            "\"provider_name\":\"DEFAULT_PROVIDER\"," +
            "\"model\":\"DEFAULT_MODEL\"," +
            "\"loc_accuracy\":13.7," +
            "\"download_classification\":1," +
            "\"upload_classification\":3," +
            "\"ping_classification\":2," +
            "\"signal_classification\":88}]" +
            "},[Content-Type:\"application/json;charset=UTF-8\"]>";
    String DEFAULT_PNG_SIZE = "forumlarge";
    byte[] DEFAULT_BYTE_ARRAY = "DEFAULT_BYTE_ARRAY".getBytes();
    String DEFAULT_VERBOSE = "1";
    String DEFAULT_SPEED_ITEMS_EMPTY = "{\"upload\":{},\"download\":{}}";
    String DEFAULT_EXPECTED_PDF_JSON_RESPONSE = "<200 OK OK,{\"file\":\"L7f2dfa9a-0755-4def-97a0-213f443793d5-20220116173631.pdf\"},[Content-Type:\"application/json\"]>";
    UUID DEFAULT_UUID = UUID.fromString("7f2dfa9a-0755-4def-97a0-213f443793d5");
    Instant DEFAULT_INSTANT = Instant.ofEpochMilli(DEFAULT_INSTANT_MILLIS);
    String DEFAULT_FILE_NAME = "L7f2dfa9a-0755-4def-97a0-213f443793d5-20220116173631";
    String DEFAULT_PDF_RESPONSE_HEADERS = "[Content-Disposition:\"attachment; filename=Measurement_result-20220116173631.pdf\", Content-Type:\"application/pdf\"]";
    MediaType DEFAULT_MEDIA_TYPE = new MediaType("text", "csv");
    String DEFAULT_FILE_CACHE_PATH = "DEFAULT_FILE_CACHE_PATH";
    Integer DEFAULT_FILE_CACHE_EXPIRATION_TERM = 23;
}
