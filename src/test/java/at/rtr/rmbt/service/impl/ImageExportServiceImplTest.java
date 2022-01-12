package at.rtr.rmbt.service.impl;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.dto.ImageGenerateDto;
import at.rtr.rmbt.utils.image.generator.FacebookThumbnailGenerator;
import at.rtr.rmbt.utils.image.generator.ForumBannerGenerator;
import at.rtr.rmbt.utils.image.generator.ForumBannerSmallGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ImageExportServiceImplTest {

    @Mock
    private FacebookThumbnailGenerator facebookThumbnailGenerator;
    @Mock
    private ForumBannerSmallGenerator forumBannerSmallGenerator;
    @Mock
    private ForumBannerGenerator forumBannerGenerator;
    @InjectMocks
    private ImageExportServiceImpl imageExportService;

    @Mock
    private ImageGenerateDto imageGenerateDto;

    @Test
    void generateImage_thumbnail_byteArray() throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new File("src/test/resources/export/banner/thumbnail_banner.png"));
        expectedByteArray(bufferedImage);
        when(imageGenerateDto.getSize()).thenReturn("thumbnail");
        when(facebookThumbnailGenerator.generateImage(imageGenerateDto)).thenReturn(bufferedImage);

        var actualResult = imageExportService.generateImage(imageGenerateDto);

        assertArrayEquals(expectedByteArray(bufferedImage), actualResult);
    }

    @Test
    void generateImage_forumsmall_byteArray() throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new File("src/test/resources/export/banner/forum_banner_small.png"));
        expectedByteArray(bufferedImage);
        when(imageGenerateDto.getSize()).thenReturn("forumsmall");
        when(forumBannerSmallGenerator.generateImage(imageGenerateDto)).thenReturn(bufferedImage);

        var actualResult = imageExportService.generateImage(imageGenerateDto);

        assertArrayEquals(expectedByteArray(bufferedImage), actualResult);
    }

    @Test
    void generateImage_default_byteArray() throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new File("src/test/resources/export/banner/forum_banner.png"));
        expectedByteArray(bufferedImage);
        when(imageGenerateDto.getSize()).thenReturn(TestConstants.DEFAULT_TEXT);
        when(forumBannerGenerator.generateImage(imageGenerateDto)).thenReturn(bufferedImage);

        var actualResult = imageExportService.generateImage(imageGenerateDto);

        assertArrayEquals(expectedByteArray(bufferedImage), actualResult);
    }

    private byte[] expectedByteArray(BufferedImage bufferedImage) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        return baos.toByteArray();
    }
}