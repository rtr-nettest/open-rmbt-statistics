package at.rtr.rmbt.utils.export;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Slf4j
public class WeasyprintPdfConverter implements PdfConverter {
    private final String path;

    public WeasyprintPdfConverter(String path) {
        this.path = path;
    }

    @Override
    public void convertHtml(Path htmlSource, Path pdfTarget) throws IOException {
        log.info("HTML source " + htmlSource.toAbsolutePath().toString());
        log.info("PDF target " + pdfTarget.toAbsolutePath().toString());
        String weasyPath = path;
        ProcessBuilder weasyProcessBuilder = new ProcessBuilder(weasyPath,
                htmlSource.toAbsolutePath().toString(),
                pdfTarget.toAbsolutePath().toString());
        Process weasyProcess = weasyProcessBuilder.start();
        try {
            weasyProcess.waitFor(10, TimeUnit.MINUTES);
            log.info("PDF generation with weasyprint finished");
        } catch (InterruptedException e) {
            weasyProcess.destroy();
            log.info("PDF generation with weasyprint terminated by timeout");
            throw new IOException(e);
        }
    }
}
