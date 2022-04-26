package at.rtr.rmbt.onstartup;

import at.rtr.rmbt.TestConstants;
import at.rtr.rmbt.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.time.Clock;
import java.time.Duration;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class OnStartUpRunnerTest {

    @MockBean
    private FileService fileService;
    @MockBean
    private Clock clock;
    private OnStartUpRunner onStartUpRunner;

    @Mock
    private File file;
    @Mock
    private File cachedFile;
    @Mock
    private File staleCachedFile;

    @BeforeEach
    void setUp() {
        onStartUpRunner = new OnStartUpRunner(fileService, clock);
        ReflectionTestUtils.setField(onStartUpRunner, "fileCachePath", TestConstants.DEFAULT_FILE_CACHE_PATH);
        ReflectionTestUtils.setField(onStartUpRunner, "fileCacheExpirationTerm", TestConstants.DEFAULT_FILE_CACHE_EXPIRATION_TERM);
    }

    @Test
    void clearFileCache_correctInvocation_fileDeleted() {
        when(fileService.openFile(TestConstants.DEFAULT_FILE_CACHE_PATH)).thenReturn(file);
        when(file.exists()).thenReturn(true);
        when(file.listFiles()).thenReturn(new File[]{cachedFile, staleCachedFile});
        when(clock.instant()).thenReturn(TestConstants.DEFAULT_INSTANT);
        when(staleCachedFile.lastModified()).thenReturn(TestConstants.DEFAULT_INSTANT_MILLIS - Duration.ofHours(TestConstants.DEFAULT_FILE_CACHE_EXPIRATION_TERM).toMillis() - 1);
        when(cachedFile.lastModified()).thenReturn(TestConstants.DEFAULT_INSTANT_MILLIS - Duration.ofHours(TestConstants.DEFAULT_FILE_CACHE_EXPIRATION_TERM).toMillis());

        onStartUpRunner.clearFileCache();

        verify(staleCachedFile).delete();
        verify(cachedFile, times(0)).delete();
    }

    @Test
    void run_dirNotExists_dirCreated() throws Exception {
        when(fileService.openFile(TestConstants.DEFAULT_FILE_CACHE_PATH)).thenReturn(file);
        when(file.exists()).thenReturn(false);

        onStartUpRunner.run(null);

        verify(file).mkdirs();
    }

    @Test
    void run_dirExists_noMkdirs() throws Exception {
        when(fileService.openFile(TestConstants.DEFAULT_FILE_CACHE_PATH)).thenReturn(file);
        when(file.exists()).thenReturn(true);

        onStartUpRunner.run(null);

        verify(file, times(0)).mkdirs();
    }
}