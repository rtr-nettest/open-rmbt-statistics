package at.rtr.rmbt.service.export.pdf.impl;

import at.rtr.rmbt.constant.Constants;
import at.rtr.rmbt.service.export.pdf.PdfGenerator;
import at.rtr.rmbt.utils.JacksonAwareSnakeCaseJavaBeanResolver;
import at.rtr.rmbt.utils.export.PdfConverter;
import at.rtr.rmbt.utils.export.PrincePdfConverter;
import at.rtr.rmbt.utils.export.WeasyprintPdfConverter;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Slf4j
@Service
public class PdfGeneratorImpl implements PdfGenerator {

    @Override
    public Path generatePdf(Template template, Map<String, Object> data, String uuid) throws IOException {
        Path pdfTarget = new File(Constants.PDF_TEMP_PATH + uuid + ".pdf").toPath();

        Context context = Context
                .newBuilder(data)
                .push(new JacksonAwareSnakeCaseJavaBeanResolver())
                .build();

        String fullTemplate = template.apply(context);
        fullTemplate = fullTemplate.replace("<script type=\"text/x-handlebars\" id=\"template\">", "");


        //create temp file
        Path htmlFile = Files.createTempFile("nt" + uuid, ".pdf.html");
        Files.write(htmlFile, fullTemplate.getBytes("utf-8"));
        log.info("Generating PDF from: " + htmlFile);

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

        boolean deleted = htmlFile.toFile().delete();
        if (!deleted) {
            log.warn("HTML file could not be deleted");
        }
        return pdfTarget;
    }
}
