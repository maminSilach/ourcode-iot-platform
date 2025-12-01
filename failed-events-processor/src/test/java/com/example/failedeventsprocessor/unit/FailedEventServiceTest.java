package com.example.failedeventsprocessor.unit;

import com.example.failedeventsprocessor.dto.response.FileResponse;
import com.example.failedeventsprocessor.service.FailedEventService;
import com.example.failedeventsprocessor.service.FileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.example.failedeventsprocessor.utils.FailedEventData.FAILED_EVENT_RECORD;
import static com.example.failedeventsprocessor.utils.FileUtils.EVENT_NULL_POINTER_FILE_RESPONSE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FailedEventServiceTest {

    private static final String FAILED_EVENT_BUCKET_NAME = "failed-event-bucket";

    private final FileService fileService = Mockito.mock(FileService.class);

    private final FailedEventService failedEventService = new FailedEventService(
            FAILED_EVENT_BUCKET_NAME, fileService
    );

    @Test
    public void event_processFailed_successful() {
        ArgumentCaptor<String> resourceCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bucketCaptor = ArgumentCaptor.forClass(String.class);
        when(fileService.uploadJsonResourceWithRetry(anyString(), anyString(), anyString())).thenAnswer(invocationOnMock -> EVENT_NULL_POINTER_FILE_RESPONSE);

        FileResponse fileResponse = failedEventService.processFailedEvent(FAILED_EVENT_RECORD);

        verify(fileService, Mockito.times(1)).uploadJsonResourceWithRetry(resourceCaptor.capture(), anyString(), bucketCaptor.capture());
        assertEquals(FAILED_EVENT_RECORD.toString(), resourceCaptor.getValue());
        assertEquals(FAILED_EVENT_BUCKET_NAME, bucketCaptor.getValue());
        assertEquals(EVENT_NULL_POINTER_FILE_RESPONSE, fileResponse);
    }
}
