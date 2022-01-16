package at.rtr.rmbt.service.export.pdf;

import com.github.jknack.handlebars.Template;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public interface PdfGenerator {

    Path generatePdf(Template template, Map<String, Object> data, String uuid) throws IOException;
}
