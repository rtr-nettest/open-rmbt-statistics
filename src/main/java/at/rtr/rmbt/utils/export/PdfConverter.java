package at.rtr.rmbt.utils.export;

import java.io.IOException;
import java.nio.file.Path;

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