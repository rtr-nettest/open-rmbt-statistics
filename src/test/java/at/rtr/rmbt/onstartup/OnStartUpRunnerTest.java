package at.rtr.rmbt.onstartup;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.time.Clock;
import java.time.Duration;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OnStartUpRunnerTest {

    @Mock
    private FileService fileService;
    @Mock
    private Clock clock;
    private OnStartUpRunner onStartUpRunner;

    @Mock
    private File file;
    @Mock
    private File cachedFile;
    @Mock
    private File staleCachedFile;
    @Mock
    private File filePdf;

    @BeforeEach
    void setUp() {
        onStartUpRunner = new OnStartUpRunner(fileService, clock);
        ReflectionTestUtils.setField(onStartUpRunner, "fileCachePath", TestConstants.DEFAULT_FILE_CACHE_PATH);
        ReflectionTestUtils.setField(onStartUpRunner, "fileCachePdfPath", TestConstants.DEFAULT_PDF_PATH);
        ReflectionTestUtils.setField(onStartUpRunner, "fileCacheExpirationTerm", TestConstants.DEFAULT_FILE_CACHE_EXPIRATION_TERM);
    }

    @Test
    void clearFileCache_correctInvocation_fileDeleted() {
        when(fileService.openFile(TestConstants.DEFAULT_FILE_CACHE_PATH)).thenReturn(file);
        when(fileService.openFile(TestConstants.DEFAULT_PDF_PATH)).thenReturn(file);
        when(file.exists()).thenReturn(true);
        when(file.listFiles()).thenReturn(new File[]{cachedFile, staleCachedFile});
        when(clock.instant()).thenReturn(TestConstants.DEFAULT_INSTANT);
        when(staleCachedFile.lastModified()).thenReturn(TestConstants.DEFAULT_INSTANT_MILLIS - Duration.ofHours(TestConstants.DEFAULT_FILE_CACHE_EXPIRATION_TERM).toMillis() - 1);
        when(cachedFile.lastModified()).thenReturn(TestConstants.DEFAULT_INSTANT_MILLIS - Duration.ofHours(TestConstants.DEFAULT_FILE_CACHE_EXPIRATION_TERM).toMillis());

        onStartUpRunner.clearFileCache();

        verify(staleCachedFile, times(2)).delete();
        verify(cachedFile, times(0)).delete();
    }

    @Test
    void run_dirNotExists_dirCreated() throws Exception {
        when(fileService.openFile(TestConstants.DEFAULT_FILE_CACHE_PATH)).thenReturn(file);
        when(file.exists()).thenReturn(false);
        when(fileService.openFile(TestConstants.DEFAULT_PDF_PATH)).thenReturn(filePdf);
        when(filePdf.exists()).thenReturn(false);

        onStartUpRunner.run(null);

        verify(file).mkdirs();
        verify(filePdf).mkdirs();
    }

    @Test
    void run_dirExists_noMkdirs() throws Exception {
        when(fileService.openFile(TestConstants.DEFAULT_FILE_CACHE_PATH)).thenReturn(file);
        when(file.exists()).thenReturn(true);
        when(fileService.openFile(TestConstants.DEFAULT_PDF_PATH)).thenReturn(filePdf);
        when(filePdf.exists()).thenReturn(true);

        onStartUpRunner.run(null);

        verify(file, times(0)).mkdirs();
        verify(file, times(0)).mkdirs();
    }
}