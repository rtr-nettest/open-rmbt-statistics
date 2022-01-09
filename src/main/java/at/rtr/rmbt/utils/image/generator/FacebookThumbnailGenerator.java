package at.rtr.rmbt.utils.image.generator;

import at.rtr.rmbt.dto.ImageGenerateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ResourceLoader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

@RequiredArgsConstructor
public class FacebookThumbnailGenerator implements ShareImageGenerator {

    private final ResourceLoader resourceLoader;

    @Override
    public BufferedImage generateImage(ImageGenerateDto imageGenerateDto) throws IOException {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        img.createGraphics();
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        BufferedImage img2 = ImageIO.read(resourceLoader.getResource("classpath:png/netztest-thumbnail.png").getInputStream());
        g.drawImage(img2, null, 0, 0);

        //Speeds
        g.setColor(Color.white);
        g.setFont(new Font("Droid Sans", Font.PLAIN, 35));
        String up = formatNumber(imageGenerateDto.getUpload(), imageGenerateDto.getLang());
        drawCenteredString(up, 25, 48, 80, 54, g);
        String down = formatNumber(imageGenerateDto.getDownload(), imageGenerateDto.getLang());
        drawCenteredString(down, 0, 0, 80, 54, g);

        g.dispose();
        return img;
    }
}
