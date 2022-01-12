package at.rtr.rmbt.utils.image.generator;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.TestUtils;
import at.rtr.rmbt.dto.ImageGenerateDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ForumBannerGeneratorTest {

    @Autowired
    private ResourceLoader resourceLoader;
    private ForumBannerGenerator forumBannerGenerator;

    @Mock
    private ImageGenerateDto imageGenerateDto;

    @BeforeEach
    void setUp() {
        forumBannerGenerator = new ForumBannerGenerator(resourceLoader);
    }

    @Test
    void generateImage_correctInvocation_BufferedImage() throws IOException {
        File expectedFile = new File("src/test/resources/export/banner/forum_banner.png");
        BufferedImage expectedResponse = ImageIO.read(expectedFile);
        when(imageGenerateDto.getLang()).thenReturn(TestConstants.DEFAULT_LANGUAGE);
        when(imageGenerateDto.getUpload()).thenReturn(TestConstants.DEFAULT_UPLOAD);
        when(imageGenerateDto.getDownload()).thenReturn(TestConstants.DEFAULT_DOWNLOAD);
        when(imageGenerateDto.getPing()).thenReturn(TestConstants.DEFAULT_PING_MS);
        when(imageGenerateDto.getTyp()).thenReturn(TestConstants.DEFAULT_NETWORK_TYPE);
        when(imageGenerateDto.getIsp()).thenReturn(TestConstants.DEFAULT_PROVIDER);
        when(imageGenerateDto.getSignal()).thenReturn(TestConstants.DEFAULT_SIGNAL_STRENGTH.toString());
        when(imageGenerateDto.getOs()).thenReturn(TestConstants.DEFAULT_PLATFORM);

        var actualResult = forumBannerGenerator.generateImage(imageGenerateDto);

        assertTrue(TestUtils.compareImages(expectedResponse, actualResult));
    }
}