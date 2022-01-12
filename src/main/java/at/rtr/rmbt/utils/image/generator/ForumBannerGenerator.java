package at.rtr.rmbt.utils.image.generator;

import at.rtr.rmbt.dto.ImageGenerateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ForumBannerGenerator implements ShareImageGenerator {

    private final ResourceLoader resourceLoader;

    @Override
    public BufferedImage generateImage(ImageGenerateDto imageGenerateDto) throws IOException {
        String unknownString = (imageGenerateDto.getLang().equals("de")) ? "unbekannt" : "unknown";
        BufferedImage img = new BufferedImage(600, 200, BufferedImage.TYPE_INT_ARGB);
        img.createGraphics();
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        BufferedImage img2 = ImageIO.read(resourceLoader.getResource("classpath:png/forumbanner_" + imageGenerateDto.getLang() + ".png").getInputStream());
        g.drawImage(img2, null, 0, 0);

        //Speeds
        g.setColor(Color.black);
        g.setFont(new Font("Droid Sans", Font.BOLD, 60));
        g.drawString(formatNumber(imageGenerateDto.getDownload(), imageGenerateDto.getLang()), 30, 105);
        g.drawString(formatNumber(imageGenerateDto.getUpload(), imageGenerateDto.getLang()), 230, 105);
        g.drawString(formatNumber(imageGenerateDto.getPing(), imageGenerateDto.getLang()), 440, 105);


        //ISP and other information
        g.setColor(Color.WHITE);
        g.setFont(new Font("Verdana", Font.BOLD, 16));

        //de
        if (imageGenerateDto.getLang().equals("de")) {
            //left
            g.drawString((imageGenerateDto.getTyp() == null) ? unknownString : imageGenerateDto.getTyp(), 110, 168);
            g.drawString((imageGenerateDto.getIsp() == null) ? unknownString : imageGenerateDto.getIsp(), 110, 191);

            //right
            g.drawString((imageGenerateDto.getSignal() == null) ? unknownString : imageGenerateDto.getSignal() + " dBm", 410, 168);
            g.drawString((imageGenerateDto.getOs() == null) ? unknownString : imageGenerateDto.getOs(), 410, 191);
        } else { //en
            //left
            g.drawString((imageGenerateDto.getTyp() == null) ? unknownString : imageGenerateDto.getTyp(), 130, 168);
            g.drawString((imageGenerateDto.getIsp() == null) ? unknownString : imageGenerateDto.getIsp(), 90, 191);

            //right
            g.drawString((imageGenerateDto.getSignal() == null) ? unknownString : imageGenerateDto.getSignal() + " dBm", 445, 168);
            g.drawString((imageGenerateDto.getOs() == null) ? unknownString : imageGenerateDto.getOs(), 445, 191);
        }

        return img;
    }

}