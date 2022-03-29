package at.rtr.rmbt.utils.image.generator;

import at.rtr.rmbt.dto.ImageGenerateDto;
import at.rtr.rmbt.utils.SignificantFormat;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.FieldPosition;
import java.text.Format;
import java.util.Locale;

public interface ShareImageGenerator {

    Color RTR_GREEN = new Color(47, 95, 0);

    default String formatNumber(double number, String lang) {
        final Locale locale = new Locale(lang);
        final Format format = new SignificantFormat(2, locale);

        final StringBuffer buf = format.format(number, new StringBuffer(), new FieldPosition(0));
        return buf.toString();
    }

    default void drawCenteredString(String s, int x, int y, int w, int h, Graphics g) {
        FontMetrics fm = g.getFontMetrics();
        x += (w - fm.stringWidth(s)) / 2;
        y += (fm.getAscent() + (h - (fm.getAscent() + fm.getDescent())) / 2);
        g.drawString(s, x, y);
    }

    BufferedImage generateImage(ImageGenerateDto imageGenerateDto) throws IOException;
}
