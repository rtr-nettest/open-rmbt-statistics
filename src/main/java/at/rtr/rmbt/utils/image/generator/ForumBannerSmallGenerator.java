package at.rtr.rmbt.utils.image.generator;

import at.rtr.rmbt.dto.ImageGenerateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ResourceLoader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

@RequiredArgsConstructor
public class ForumBannerSmallGenerator implements ShareImageGenerator {

    private final ResourceLoader resourceLoader;

    @Override
    public BufferedImage generateImage(ImageGenerateDto imageGenerateDto) throws IOException {
        String unknownString = (imageGenerateDto.getLang().equals("de")) ? "unbekannt" : "unknown";
        BufferedImage img = new BufferedImage(390, 130, BufferedImage.TYPE_INT_ARGB);
        img.createGraphics();
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        BufferedImage img2 = ImageIO.read(resourceLoader.getResource("classpath:png/forumsmall_" + imageGenerateDto.getLang() + ".png").getInputStream());
        g.drawImage(img2, null, 0, 0);

        //Speeds
        g.setColor(Color.black);
        g.setFont(new Font("Droid Sans", Font.BOLD, 40));
        drawCenteredString(formatNumber(imageGenerateDto.getDownload(), imageGenerateDto.getLang()), 0, 20, 130, 65, g);
        drawCenteredString(formatNumber(imageGenerateDto.getUpload(), imageGenerateDto.getLang()), 130, 20, 130, 65, g);
        drawCenteredString(formatNumber(imageGenerateDto.getPing(), imageGenerateDto.getLang()), 260, 20, 130, 65, g);


        //ISP and other information
        g.setColor(Color.WHITE);
        g.setFont(new Font("Verdana", Font.BOLD, 10));

        //de
        if (imageGenerateDto.getLang().equals("de")) {
            //left
            g.drawString((imageGenerateDto.getTyp() == null) ? unknownString : imageGenerateDto.getTyp(), 73, 109);
            g.drawString((imageGenerateDto.getIsp() == null) ? unknownString : imageGenerateDto.getIsp(), 73, 124);

            //right
            g.drawString((imageGenerateDto.getSignal() == null) ? "" : imageGenerateDto.getSignal() + " dBm", 270, 109);
            g.drawString((imageGenerateDto.getOs() == null) ? unknownString : imageGenerateDto.getOs(), 270, 124);

            //hide signal caption if signal is null
            if (imageGenerateDto.getSignal() == null) {
                g.setColor(new Color(89, 178, 0));
                g.fillRect(195, 98, 71, 13);

            }

        } else { //en
            //left
            g.drawString((imageGenerateDto.getTyp() == null) ? unknownString : imageGenerateDto.getTyp(), 83, 109);
            g.drawString((imageGenerateDto.getIsp() == null) ? unknownString : imageGenerateDto.getIsp(), 60, 124);

            //right
            g.drawString((imageGenerateDto.getSignal() == null) ? "" : imageGenerateDto.getSignal() + " dBm", 290, 109);
            g.drawString((imageGenerateDto.getOs() == null) ? unknownString : imageGenerateDto.getOs(), 290, 124);

            //hide signal caption if signal is null
            if (imageGenerateDto.getSignal() == null) {
                g.setColor(new Color(89, 178, 0));
                g.fillRect(195, 98, 90, 13);

            }
        }

        g.dispose();

        return img;
    }
}
