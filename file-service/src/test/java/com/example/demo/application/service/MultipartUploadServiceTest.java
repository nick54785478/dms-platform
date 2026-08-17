package com.example.demo.application.service;

import com.example.demo.application.port.in.MultipartUploadUseCase.InitiateMultipartResult;
import com.example.demo.application.port.out.BlobStorageManagerPort;
import com.example.demo.application.port.out.FileMetadataRepositoryPort;
import com.example.demo.application.shared.command.MultipartUploadCommand;
import com.example.demo.domain.file.aggregate.root.FileMetadata;
import com.example.demo.domain.file.aggregate.vo.FileStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MultipartUploadServiceTest {

    @Mock
    private FileMetadataRepositoryPort fileMetadataRepositoryPort;

    @Mock
    private BlobStorageManagerPort blobStorageManagerPort;

    private MultipartUploadService multipartUploadService;

    private final String tempBucket = "temp-bucket";

    @BeforeEach
    void setUp() {
        multipartUploadService = new MultipartUploadService(
                blobStorageManagerPort,
                fileMetadataRepositoryPort,
                tempBucket
        );
    }

    @Test
    void initiateMultipartUpload_ShouldInitiateAndSaveMetadata() throws Exception {
        // Arrange
        MultipartUploadCommand.InitiateCommand command = new MultipartUploadCommand.InitiateCommand(
                "tenant-A", "large_video.mp4", "VIDEO", "video/mp4", 5000000L, null, null
        );

        when(blobStorageManagerPort.initiateMultipartUpload(eq(tempBucket), anyString())).thenReturn("upload-id-123");
        when(fileMetadataRepositoryPort.save(any(FileMetadata.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        InitiateMultipartResult result = multipartUploadService.initiateMultipartUpload(command);

        // Assert
        assertNotNull(result);
        assertEquals("upload-id-123", result.uploadId());
        assertNotNull(result.fileId());

        verify(blobStorageManagerPort, times(1)).initiateMultipartUpload(eq(tempBucket), anyString());
        
        ArgumentCaptor<FileMetadata> metadataCaptor = ArgumentCaptor.forClass(FileMetadata.class);
        verify(fileMetadataRepositoryPort, times(1)).save(metadataCaptor.capture());
        assertEquals(FileStatus.UNBOUND, metadataCaptor.getValue().getStatus());
    }

    @Test
    void getPresignedPartUrl_ShouldReturnUrl() throws Exception {
        // Arrange
        String fileId = "file-123";
        String uploadId = "upload-id-123";
        
        FileMetadata metadata = FileMetadata.create("tenant-A", "video.mp4", "VIDEO", "video/mp4", 1024L, null, null);
        when(fileMetadataRepositoryPort.findById(fileId)).thenReturn(Optional.of(metadata));
        when(blobStorageManagerPort.getPresignedUploadPartUrl(eq(tempBucket), anyString(), eq(uploadId), eq(1), eq(15)))
                .thenReturn("http://presigned-part-url");

        // Act
        String url = multipartUploadService.getPresignedPartUrl(fileId, uploadId, 1, 15);

        // Assert
        assertEquals("http://presigned-part-url", url);
        verify(blobStorageManagerPort, times(1)).getPresignedUploadPartUrl(eq(tempBucket), anyString(), eq(uploadId), eq(1), eq(15));
    }

    @Test
    void completeMultipartUpload_ShouldCallCompleteAndSaveMetadata() throws Exception {
        // Arrange
        String fileId = "file-123";
        String uploadId = "upload-id-123";
        Map<Integer, String> eTags = new HashMap<>();
        eTags.put(1, "etag-1");
        
        MultipartUploadCommand.CompleteCommand command = new MultipartUploadCommand.CompleteCommand(fileId, uploadId, eTags);

        FileMetadata metadata = FileMetadata.create("tenant-A", "video.mp4", "VIDEO", "video/mp4", 1024L, null, null);
        when(fileMetadataRepositoryPort.findById(fileId)).thenReturn(Optional.of(metadata));
        when(fileMetadataRepositoryPort.save(any(FileMetadata.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        FileMetadata resultMetadata = multipartUploadService.completeMultipartUpload(command);

        // Assert
        assertNotNull(resultMetadata);
        verify(blobStorageManagerPort, times(1)).completeMultipartUpload(eq(tempBucket), anyString(), eq(uploadId), eq(eTags));
        verify(fileMetadataRepositoryPort, times(1)).save(metadata);
    }

    @Test
    void abortMultipartUpload_ShouldCallAbortAndMarkAsDeleted() throws Exception {
        // Arrange
        String fileId = "file-123";
        String uploadId = "upload-id-123";
        
        MultipartUploadCommand.AbortCommand command = new MultipartUploadCommand.AbortCommand(fileId, uploadId);

        FileMetadata metadata = FileMetadata.create("tenant-A", "video.mp4", "VIDEO", "video/mp4", 1024L, null, null);
        when(fileMetadataRepositoryPort.findById(fileId)).thenReturn(Optional.of(metadata));

        // Act
        multipartUploadService.abortMultipartUpload(command);

        // Assert
        assertEquals(FileStatus.DELETED, metadata.getStatus()); // Should be marked as deleted
        verify(blobStorageManagerPort, times(1)).abortMultipartUpload(eq(tempBucket), anyString(), eq(uploadId));
        verify(fileMetadataRepositoryPort, times(1)).save(metadata);
    }
}
