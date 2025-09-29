package com.example.failedeventsprocessor.unit;

import com.example.failedeventsprocessor.dto.response.FileResponse;
import com.example.failedeventsprocessor.exception.AwsException;
import com.example.failedeventsprocessor.mapper.FileMapperImpl;
import com.example.failedeventsprocessor.service.FileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URL;
import java.time.LocalDateTime;

import static com.example.failedeventsprocessor.utils.FailedEventData.FAILED_EVENT_RECORD;
import static com.example.failedeventsprocessor.utils.FileUtils.AWS_HOST;
import static com.example.failedeventsprocessor.utils.FileUtils.AWS_REGION;
import static com.example.failedeventsprocessor.utils.FileUtils.EVENT_NULL_POINTER_FILE_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {

    private static final String FAILED_EVENT_BUCKET_NAME = "failed-event-bucket";

    private final FileMapperImpl fileMapper = Mockito.spy(FileMapperImpl.class);

    private final S3Client s3Client = Mockito.mock(S3Client.class);

    private final S3Utilities s3Utilities = Mockito.mock(S3Utilities.class);

    private final FileService fileService = new FileService(
            AWS_HOST, AWS_REGION, s3Client, fileMapper
    );

    @Test
    public void file_uploadJsonResourceWithRetry_successful() {
        String expectedUrl = AWS_HOST + "/" + FAILED_EVENT_BUCKET_NAME + "/" + EVENT_NULL_POINTER_FILE_KEY;
        InOrder inOrder = Mockito.inOrder(fileMapper, s3Client, s3Utilities);

        when(s3Client.utilities()).thenAnswer(invocationOnMock -> s3Utilities);
        when(s3Utilities.getUrl(any(GetUrlRequest.class))).thenAnswer(invocationOnMock -> new URL(expectedUrl));

        FileResponse fileResponse = fileService.uploadJsonResourceWithRetry(FAILED_EVENT_RECORD.toString(), EVENT_NULL_POINTER_FILE_KEY, FAILED_EVENT_BUCKET_NAME);

        inOrder.verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        inOrder.verify(s3Client).utilities();
        inOrder.verify(s3Utilities).getUrl(any(GetUrlRequest.class));
        inOrder.verify(fileMapper).toFileResponse(anyString(), anyString());

        verify(s3Client, Mockito.times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(s3Client, Mockito.times(1)).utilities();
        verify(s3Utilities, Mockito.times(1)).getUrl(any(GetUrlRequest.class));
        verify(fileMapper, Mockito.times(1)).toFileResponse(anyString(), anyString());

        assertEquals(EVENT_NULL_POINTER_FILE_KEY, fileResponse.key);
        assertEquals(expectedUrl, fileResponse.url);
        assertTrue(fileResponse.date.isBefore(LocalDateTime.now()));
    }

    @Test
    public void file_uploadJsonResourceWithRetry_exception() {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenThrow(RuntimeException.class);
        assertThrows(
                AwsException.class,
                () -> fileService.uploadJsonResourceWithRetry(FAILED_EVENT_RECORD.toString(), EVENT_NULL_POINTER_FILE_KEY, FAILED_EVENT_BUCKET_NAME)
        );
    }
}
