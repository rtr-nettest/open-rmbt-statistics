package at.rtr.rmbt.utils.export;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;

@Slf4j
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
