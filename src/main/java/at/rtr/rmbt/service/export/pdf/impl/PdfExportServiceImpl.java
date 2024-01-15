package at.rtr.rmbt.service.export.pdf.impl;

import at.rtr.rmbt.constant.Constants;
import at.rtr.rmbt.exception.InvalidRequestParameterException;
import at.rtr.rmbt.repository.OpenTestRepository;
import at.rtr.rmbt.response.OpenTestDetailsDTO;
import at.rtr.rmbt.response.opentest.OpenTestDTO;
import at.rtr.rmbt.response.opentest.OpenTestSearchResponse;
import at.rtr.rmbt.service.FileService;
import at.rtr.rmbt.service.UuidGenerator;
import at.rtr.rmbt.service.export.pdf.PdfExportService;
import at.rtr.rmbt.service.export.pdf.PdfGenerator;
import at.rtr.rmbt.utils.ConvertUtils;
import at.rtr.rmbt.utils.ExtendedHandlebars;
import at.rtr.rmbt.utils.QueryParser;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfExportServiceImpl implements PdfExportService {
    public static final int MAX_RESULTS = 1000; //max results for pdf

    private final OpenTestRepository openTestRepository;
    private final PdfGenerator pdfGenerator;
    private final ResourceLoader resourceLoader;
    private final UuidGenerator uuidGenerator;
    private final Clock clock;
    private final FileService fileService;

    @Value("${app.fileCache.pdfPath}")
    private String pdfPath;

    @Override
    public ResponseEntity<Object> generatePdf(String acceptHeader, MultiValueMap<String, String> parameters, String lang) {
        String language = getCurrentLanguage(lang);
        ResourceBundle labels = ResourceBundle.getBundle("SystemMessages", new Locale(language));
        //load template
        String pdfFilename = labels.getString("RESULT_PDF_FILENAME");
        Handlebars handlebars = new ExtendedHandlebars();
        Template template = null;
        boolean certifiedMeasurement;
        try {
            String html;
            if (parameters.size() > 1 && !Strings.isNullOrEmpty(parameters.getFirst("first"))) {
                //use different template for certified measurement protocol
                html = com.google.common.io.Resources.toString(getClass().getClassLoader().getResource("export/export_zert.hbs.html"), Charsets.UTF_8);
                pdfFilename = labels.getString("RESULT_PDF_FILENAME_CERTIFIED");
                certifiedMeasurement = true;
            } else {
                html = com.google.common.io.Resources.toString(getClass().getClassLoader().getResource("export/export.hbs.html"), Charsets.UTF_8);
                pdfFilename = labels.getString("RESULT_PDF_FILENAME");
                certifiedMeasurement = false;
            }
            template = handlebars.compileInline(html);
        } catch (IOException e) {
            e.printStackTrace();
            certifiedMeasurement = false;
        }

        final QueryParser qp = new QueryParser();
        //parse the input query
        final List<String> invalidElements = qp.parseQuery(parameters);
        //only accept open_test_uuid and loop_uuid as input parameters
        if (qp.getWhereParams().size() < 1 ||
                (qp.getWhereParams().size() == 1 && (!qp.getWhereParams().containsKey("open_test_uuid") &&
                        !qp.getWhereParams().containsKey("test_uuid") &&
                        !qp.getWhereParams().containsKey("loop_uuid")))) {
            throw new InvalidRequestParameterException("submit open_test_uuid or loop_uuid");
        }


        OpenTestSearchResponse searchResult = openTestRepository.getOpenTestSearchResults(qp, 0, MAX_RESULTS, new HashSet<>());

        Map<String, Object> data = new HashMap<>();

        //date handling
        Date generationDate = new Date(clock.instant().toEpochMilli());
        SimpleDateFormat sdf = new SimpleDateFormat("d.M.yyyy H:mm:ss", Locale.GERMAN);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Vienna"));
        SimpleDateFormat filenameDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.GERMAN);
        filenameDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Vienna"));
        String filenameDatePart = filenameDateFormat.format(generationDate);
        data.put("date", sdf.format(generationDate));

        //make tests accessible to handlebars
        List<OpenTestDTO> testResults = searchResult.getResults();
        Collections.reverse(testResults);
        data.put("tests", testResults);

        //add all params to the model
        //if not an array, don't make it one
        parameters.keySet().stream().forEach(k -> {
            if (parameters.get(k).size() > 1) {
                String arrayName = k.replace("[]","") + "Arr"; //handlebars does not support square bracket
                data.put(arrayName, parameters.get(k));
            }
            else {
                data.put(k, parameters.getFirst(k));
            }
        });

        //if no measurements - don't generate the application
        if (searchResult.getResults() == null || searchResult.getResults().isEmpty()) {
            return ResponseEntity.notFound()
                    .build();
        }

        //get details for single results - set more detailled info
        ListIterator<OpenTestDTO> testIterator = testResults.listIterator();
        while (testIterator.hasNext()) {
            OpenTestDTO result = testIterator.next();
            OpenTestDetailsDTO singleTest = openTestRepository.getOpenTestByUuid(ConvertUtils.formatOpenTestUuid(result.getOpenTestUuid()));
            testIterator.set(singleTest);
        }

        addLogoPngFile(data);
        addTranslationFiles(language, labels, data);

        try {
            String uuid = uuidGenerator.generateNewUuid().toString();
            Path pdfTarget = pdfGenerator.generatePdf(template, data, uuid);
            return getObjectResponseEntity(acceptHeader, certifiedMeasurement, pdfFilename, filenameDatePart, uuid, pdfTarget);

        } catch (IOException e) {
            log.error("Error", e);
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        }
    }

    @Override
    public ResponseEntity<Object> loadPdf(String fileName, String lang) {
        ResourceBundle labels = getLabelsByLang(lang);

        char discriminatorLetter = fileName.charAt(0);
        fileName = fileName.substring(1);

        //keep date, if any
        String[] filenameParts = fileName.split("-");
        String filenameDatePart = "";
        if (filenameParts[filenameParts.length - 1].length() == 14) {
            //subtract from filename date part + "minus"
            fileName = fileName.substring(0, fileName.length() - 15);
            filenameDatePart = "-" + filenameParts[filenameParts.length - 1];
        }

        File retFile = fileService.openFile(pdfPath + File.separator + fileName + ".pdf");

        if (!retFile.exists()) {
            throw new RuntimeException("File not found");
        }

        //different filenames for certified measurement vs loop mode pdf
        String pdfFilename;
        if (discriminatorLetter == 'C') {
            pdfFilename = labels.getString("RESULT_PDF_FILENAME_CERTIFIED");
        } else {
            pdfFilename = labels.getString("RESULT_PDF_FILENAME");
        }

        try {
            byte[] out = Files.readAllBytes(retFile.toPath());
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=" + pdfFilename + filenameDatePart + ".pdf")
                    .contentType(new MediaType("application", "pdf"))
                    .body(out);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .build();
        }
    }

    private ResourceBundle getLabelsByLang(String lang) {
        String currentLanguage = getCurrentLanguage(lang);
        return ResourceBundle.getBundle("SystemMessages", new Locale(currentLanguage));
    }

    private String getCurrentLanguage(String lang) {
        return Optional.ofNullable(lang)
                .filter(Constants.RMBT_SUPPORTED_LANGUAGES::contains)
                .orElse(Constants.RMBT_DEFAULT_LANGUAGE);
    }

    private void addLogoPngFile(Map<String, Object> data) {
        //add further parameters, i.e. ,logos
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = resourceLoader.getResource("classpath:png/logo.png").getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (resourceAsStream != null) {
            try {
                BufferedImage img2 = ImageIO.read(resourceAsStream);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(img2, "png", os);
                os.flush();
                String imageAsBase64 = org.apache.commons.codec.binary.Base64.encodeBase64String(os.toByteArray());
                data.put("logo", imageAsBase64);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addTranslationFiles(String language, ResourceBundle labels, Map<String, Object> data) {
        //add translation files
        if (labels != null) {
            Map<String, String> labelsMap = new HashMap<>();
            Set<String> keys = labels.keySet();
            for (String key : keys) {
                if (key.startsWith("key_")) {
                    labelsMap.put(key.substring(4), labels.getString(key));
                }
            }
            labelsMap.put("lang", language);
            data.put("Lang", labelsMap);
        }
    }

    private ResponseEntity<Object> getObjectResponseEntity(String acceptHeader, boolean certifiedMeasurement, String pdfFilename, String filenameDatePart, String uuid, Path pdfTarget) throws IOException {
        //depending on Accepts-Header, return file or json with link to file
        if (acceptHeader.contains("application/json")) {
            JSONObject retJson = new JSONObject();
            retJson.put("file", (certifiedMeasurement ? "C" : "L") + uuid + "-" + filenameDatePart + ".pdf");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(retJson.toString());
        } else {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + pdfFilename + "-" + filenameDatePart + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(Files.readAllBytes(pdfTarget));
        }
    }
}
