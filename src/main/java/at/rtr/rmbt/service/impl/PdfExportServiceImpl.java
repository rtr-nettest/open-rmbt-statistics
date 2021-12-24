package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.constant.Constants;
import at.rtr.rmbt.exception.InvalidRequestParameterException;
import at.rtr.rmbt.repository.OpenTestRepository;
import at.rtr.rmbt.response.OpenTestDetailsDTO;
import at.rtr.rmbt.response.opentest.OpenTestDTO;
import at.rtr.rmbt.response.opentest.OpenTestSearchResponse;
import at.rtr.rmbt.service.PdfExportService;
import at.rtr.rmbt.utils.ConvertUtils;
import at.rtr.rmbt.utils.ExtendedHandlebars;
import at.rtr.rmbt.utils.QueryParser;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.google.common.base.CaseFormat;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
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
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfExportServiceImpl implements PdfExportService {
    public static final int MAX_RESULTS = 1000; //max results for pdf

    private final OpenTestRepository openTestRepository;

    @Value("classpath:export/export_zert.hbs.html")
    private Resource exportZertHbcHtml;

    @Value("classpath:resource.txt")
    private Resource resource;

    @Override
    public ResponseEntity<Object> exportPdf(String acceptHeader, MultiValueMap<String, String> parameters) {
        Locale currentLocale = Locale.ENGLISH;

        //load locale, if possible
        String language = Constants.RMBT_DEFAULT_LANGUAGE;
        ResourceBundle labels = ResourceBundle.getBundle("SystemMessages", currentLocale);

        if (parameters.containsKey("lang")) {
            if (Constants.RMBT_SUPPORTED_LANGUAGES.contains(parameters.getFirst("lang"))) {
                labels = ResourceBundle.getBundle("SystemMessages", new Locale(parameters.getFirst("lang")));
            }
        }

        String tempPath = Constants.PDF_TEMP_PATH;
        //allow only fetching files
        if (parameters.containsKey("filename")) {
            String fileName = parameters.getFirst("filename");

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

            File retFile = new File(tempPath + fileName + ".pdf");

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

        //load template
        Handlebars handlebars = new ExtendedHandlebars();
        Template template = null;
        boolean certifiedMeasurement;
        String pdfFilename = labels.getString("RESULT_PDF_FILENAME");
        try {
            String html;
            if (parameters.size() > 1 && !Strings.isNullOrEmpty(parameters.get("first").get(0))) {
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
        Date generationDate = new Date();
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
        data.putAll(parameters);
        //data.putAll(multivalueParams);

        //if no measurements - don't generate the application
        if (searchResult.getResults() == null || searchResult.getResults().isEmpty()) {
            return ResponseEntity.notFound()
                    .build();
        }

        //get details for single results - set more detailled info
        ListIterator<OpenTestDTO> testIterator = testResults.listIterator();
        while (testIterator.hasNext()) {
            OpenTestDTO result = testIterator.next();
            OpenTestDetailsDTO singleTest = openTestRepository.getOpenTestByUuid(ConvertUtils.formatOpenTestUuid(result.getOpenTestUuid()), 0);
            testIterator.set(singleTest);
        }

        //add further parameters, i.e. logos
        InputStream resourceAsStream = getClass().getResourceAsStream("logo.png");
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

        String fullTemplate;
        try {
            Context context = Context
                    .newBuilder(data)
                    .push(new JacksonAwareSnakeCaseJavaBeanResolver())
                    .build();
            fullTemplate = template.apply(context);
            fullTemplate = fullTemplate.replace("<script type=\"text/x-handlebars\" id=\"template\">", "");

            String uuid = UUID.randomUUID().toString();
            //create temp file
            Path htmlFile = Files.createTempFile("nt" + uuid, ".pdf.html");
            Files.write(htmlFile, fullTemplate.getBytes("utf-8"));
            log.info("Generating PDF from: " + htmlFile);

            Path pdfTarget = new File(tempPath + uuid + ".pdf").toPath();

            PdfConverter pdfConverter;
            switch (Constants.PDF_CONVERTER) {
                case "weasyprint":
                    pdfConverter = new WeasyprintPdfConverter(Constants.WEASYPRINT_PATH);
                    break;
                case "prince":
                    pdfConverter = new PrincePdfConverter(Constants.PRINCE_PATH);
                    break;
                default:
                    throw new RuntimeException("invalid pdfgenerator");
            }

            pdfConverter.convertHtml(htmlFile, pdfTarget);
            log.info("PDF generated: " + pdfTarget);

            //delete html file, as not longer needed
            boolean deleted = htmlFile.toFile().delete();
            if (!deleted) {
                log.warn("HTML file could not be deleted");
            }


            //depending on Accepts-Header, return file or json with link to file
            if (acceptHeader.equals("application/json")) {
                JSONObject retJson = new JSONObject();
                retJson.put("file", (certifiedMeasurement ? "C" : "L") + uuid + "-" + filenameDatePart + ".pdf");

                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(retJson.toString());
            } else {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + pdfFilename + "-" + filenameDatePart + ".pdf")
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(pdfTarget.toFile());
            }
        } catch (IOException e) {
            log.error("Error", e);
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        }
    }

    public static class JacksonAwareSnakeCaseJavaBeanResolver extends JavaBeanValueResolver {
        public JacksonAwareSnakeCaseJavaBeanResolver() {
            super();
        }

        /**
         * Get the property name for a given method,
         * either from the @JsonProperty annotation, or from translating to snake_case
         *
         * @param member
         * @return
         */
        @Override
        protected String memberName(final java.lang.reflect.Method member) {
            if (member.getDeclaringClass().isInstance(new HashMap<>())) {
                return super.memberName(member);
            }

            JsonProperty annotation = member.getAnnotation(JsonProperty.class);
            if (annotation != null) {
                return annotation.value();
            }

            String withoutGetterIs = super.memberName(member);

            String otherName = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE).convert(withoutGetterIs);
            return otherName;
        }

        /**
         * Is invoked to check if the methode actually is a getter or setter
         *
         * @param method Method to check
         * @param name   Translated name of the method (already being snake_case)
         * @return
         */
        @Override
        public boolean matches(final java.lang.reflect.Method method, final String name) {
            if (method.getDeclaringClass().isInstance(new HashMap<>())) {
                return super.matches(method, name);
            }

            //if it matches the annotation - it matches
            JsonProperty annotation = method.getAnnotation(JsonProperty.class);
            if (annotation != null && name.equals(annotation.value())) {
                return true;
            }

            //name is here the "translated" name - translate back to get if it matches
            String otherName = CaseFormat.LOWER_UNDERSCORE.converterTo(CaseFormat.LOWER_CAMEL).convert(name);
            return super.matches(method, otherName) || super.matches(method, name);
        }
    }

    public interface PdfConverter {
        /**
         * Convert the given HTML source file to given target pdf
         *
         * @param htmlSource
         * @param pdfTarget
         * @throws IOException
         */
        void convertHtml(Path htmlSource, Path pdfTarget) throws IOException;
    }

    public class PrincePdfConverter implements PdfConverter {
        private final String path;

        public PrincePdfConverter(String path) {
            this.path = path;
        }

        @Override
        public void convertHtml(Path htmlSource, Path pdfTarget) throws IOException {
            String princePath = path;
            ProcessBuilder princeProcessBuilder = new ProcessBuilder(princePath,
                    htmlSource.toAbsolutePath().toString(),
                    "-o",
                    pdfTarget.toAbsolutePath().toString());
            Process princeProcess = princeProcessBuilder.start();
            try {
                princeProcess.waitFor();
                log.info("PDF generation with Prince finished");
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }
    }

    public class WeasyprintPdfConverter implements PdfConverter {
        private final String path;

        public WeasyprintPdfConverter(String path) {
            this.path = path;
        }

        @Override
        public void convertHtml(Path htmlSource, Path pdfTarget) throws IOException {
            String weasyPath = path;
            ProcessBuilder weasyProcessBuilder = new ProcessBuilder(weasyPath,
                    htmlSource.toAbsolutePath().toString(),
                    pdfTarget.toAbsolutePath().toString());
            Process weasyProcess = weasyProcessBuilder.start();
            try {
                weasyProcess.waitFor();
                log.info("PDF generation with weasyprint finished");
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }
    }
}
