package at.rtr.rmbt.utils.image.generator;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.TestUtils;
import at.rtr.rmbt.dto.ImageGenerateDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class FacebookThumbnailGeneratorTest {

    @Autowired
    private ResourceLoader resourceLoader;
    private FacebookThumbnailGenerator facebookThumbnailGenerator;

    @Mock
    private ImageGenerateDto imageGenerateDto;

    @BeforeEach
    void setUp() {
        facebookThumbnailGenerator = new FacebookThumbnailGenerator(resourceLoader);
    }

    @Test
    @Disabled("Need library on gitlab")
    void generateImage_correctInvocation_BufferedImage() throws IOException {
        File expectedFile = new File("src/test/resources/export/banner/thumbnail_banner.png");
        BufferedImage expectedResponse = ImageIO.read(expectedFile);
        when(imageGenerateDto.getLang()).thenReturn(TestConstants.DEFAULT_LANGUAGE);
        when(imageGenerateDto.getUpload()).thenReturn(TestConstants.DEFAULT_UPLOAD);
        when(imageGenerateDto.getDownload()).thenReturn(TestConstants.DEFAULT_DOWNLOAD);

        var actualResult = facebookThumbnailGenerator.generateImage(imageGenerateDto);

        assertTrue(TestUtils.compareImages(expectedResponse, actualResult));
    }
}