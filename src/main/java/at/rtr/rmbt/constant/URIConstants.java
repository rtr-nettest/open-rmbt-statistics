package at.rtr.rmbt.constant;

public interface URIConstants {
    String VERSION = "/version";
    String STATISTICS = "/statistics";
    String OPEN_TEST_SEARCH = "/opentests/search";
    String OPEN_TEST = "/opentests";
    String EXPORT_PNG = "/{lang}/{open_test_uuid}/{size}.png";
    String OPEN_TEST_BY_UUID = "/opentests/O{open_test_uuid}";
    String EXPORT_OPEN_DATA = "/export/netztest-opendata-{year}-{month}.{format}";
    String EXPORT_PDF = "/export/pdf";
    String EXPORT_PDF_LANG = "/export/pdf/{lang}";
    String EXPORT_PDF_FILENAME = "/export/pdf/{fileName}.pdf";
    String EXPORT_PDF_LANG_FILENAME = "/export/pdf/{lang}/{fileName}.pdf";
}
