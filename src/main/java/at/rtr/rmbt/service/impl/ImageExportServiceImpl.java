package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.dto.ImageGenerateDto;
import at.rtr.rmbt.service.ImageExportService;
import at.rtr.rmbt.utils.image.generator.FacebookThumbnailGenerator;
import at.rtr.rmbt.utils.image.generator.ForumBannerGenerator;
import at.rtr.rmbt.utils.image.generator.ForumBannerSmallGenerator;
import at.rtr.rmbt.utils.image.generator.ShareImageGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ImageExportServiceImpl implements ImageExportService {

    private final FacebookThumbnailGenerator facebookThumbnailGenerator;
    private final ForumBannerSmallGenerator forumBannerSmallGenerator;
    private final ForumBannerGenerator forumBannerGenerator;

    @Override
    public byte[] generateImage(ImageGenerateDto imageGenerateDto) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ShareImageGenerator generator;
            if (imageGenerateDto.getSize().equals("thumbnail")) {
                generator = facebookThumbnailGenerator;
            } else if (imageGenerateDto.getSize().equals("forumsmall")) {
                generator = forumBannerSmallGenerator;
            } else {
                generator = forumBannerGenerator;
            }

            BufferedImage img = generator.generateImage(imageGenerateDto);
            ImageIO.write(img, "png", byteArrayOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return byteArrayOutputStream.toByteArray();
    }
}
