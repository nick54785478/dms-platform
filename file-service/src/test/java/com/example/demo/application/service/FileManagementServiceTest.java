package com.example.demo.application.service;

import com.example.demo.application.port.in.ManageFileUseCase;
import com.example.demo.application.port.out.BlobStorageManagerPort;
import com.example.demo.application.port.out.FileMetadataRepositoryPort;
import com.example.demo.application.shared.command.PresignedUrlCommand;
import com.example.demo.application.shared.command.UploadFileCommand;
import com.example.demo.application.shared.dto.PresignedUrlGeneratedResult;
import com.example.demo.domain.file.aggregate.root.FileMetadata;
import com.example.demo.domain.file.aggregate.vo.FileStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileManagementServiceTest {

    @Mock
    private FileMetadataRepositoryPort fileMetadataRepositoryPort;

    @Mock
    private BlobStorageManagerPort blobStorageManagerPort;

    private FileManagementService fileManagementService;

    private final String tempBucket = "temp-bucket";
    private final String permanentBucket = "permanent-bucket";

    @BeforeEach
    void setUp() {
        fileManagementService = new FileManagementService(
                blobStorageManagerPort,
                fileMetadataRepositoryPort,
                tempBucket,
                permanentBucket
        );
    }

    @Test
    void uploadFile_ShouldUploadToTempBucketAndSaveMetadata() throws Exception {
        // Arrange
        MultipartFile mockFile = new MockMultipartFile("file", "test.pdf", "application/pdf", "dummy content".getBytes());
        UploadFileCommand command = new UploadFileCommand("tenant-A", "test.pdf", "DOCUMENT", "application/pdf", 1024L, "checksum", null, mockFile);

        when(fileMetadataRepositoryPort.save(any(FileMetadata.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        FileMetadata savedMetadata = fileManagementService.uploadFile(command);

        // Assert
        assertNotNull(savedMetadata);
        assertEquals(FileStatus.UNBOUND, savedMetadata.getStatus());
        assertEquals("tenant-A", savedMetadata.getTenantId());
        
        verify(blobStorageManagerPort, times(1)).uploadFile(eq(tempBucket), eq(mockFile), eq(""), anyString());
        verify(fileMetadataRepositoryPort, times(1)).save(any(FileMetadata.class));
    }

    @Test
    void generatePresignedUploadUrl_ShouldReturnUrlAndSaveUnboundMetadata() throws Exception {
        // Arrange
        PresignedUrlCommand command = new PresignedUrlCommand("tenant-A", "test.pdf", "DOCUMENT", "application/pdf", 1024L, "checksum", null, 15);
        
        when(blobStorageManagerPort.getPresignedUploadUrl(eq(tempBucket), anyString(), eq(15))).thenReturn("http://presigned-upload-url");
        when(fileMetadataRepositoryPort.save(any(FileMetadata.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PresignedUrlGeneratedResult result = fileManagementService.generatePresignedUploadUrl(command);

        // Assert
        assertNotNull(result);
        assertEquals("http://presigned-upload-url", result.url());
        
        verify(blobStorageManagerPort, times(1)).getPresignedUploadUrl(eq(tempBucket), anyString(), eq(15));
        verify(fileMetadataRepositoryPort, times(1)).save(any(FileMetadata.class));
    }

    @Test
    void generatePresignedDownloadUrl_WhenFileNotFound_ShouldThrowException() throws Exception {
        // Arrange
        when(fileMetadataRepositoryPort.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> fileManagementService.generatePresignedDownloadUrl("non-existent-id", true, 15));
        
        verify(blobStorageManagerPort, never()).getPresignedDownloadUrl(anyString(), anyString(), anyString(), anyBoolean(), anyInt());
    }
    
    @Test
    void generatePresignedDownloadUrl_WhenFoundAndUnbound_ShouldReturnTempBucketUrl() throws Exception {
        // Arrange
        FileMetadata metadata = FileMetadata.create("tenant-A", "test.pdf", "DOCUMENT", "application/pdf", 1024L, "checksum", null);
        when(fileMetadataRepositoryPort.findById(anyString())).thenReturn(Optional.of(metadata));
        when(blobStorageManagerPort.getPresignedDownloadUrl(eq(tempBucket), anyString(), eq("test.pdf"), eq(true), eq(15))).thenReturn("http://presigned-download-url");

        // Act
        String url = fileManagementService.generatePresignedDownloadUrl(metadata.getId(), true, 15);

        // Assert
        assertEquals("http://presigned-download-url", url);
        verify(blobStorageManagerPort, times(1)).getPresignedDownloadUrl(eq(tempBucket), anyString(), eq("test.pdf"), eq(true), eq(15));
    }
    
    @Test
    void generatePresignedDownloadUrl_WhenFoundAndBound_ShouldReturnPermanentBucketUrl() throws Exception {
        // Arrange
        FileMetadata metadata = FileMetadata.create("tenant-A", "test.pdf", "DOCUMENT", "application/pdf", 1024L, "checksum", null);
        metadata.markAsBound();
        when(fileMetadataRepositoryPort.findById(anyString())).thenReturn(Optional.of(metadata));
        when(blobStorageManagerPort.getPresignedDownloadUrl(eq(permanentBucket), anyString(), eq("test.pdf"), eq(false), eq(15))).thenReturn("http://presigned-preview-url");

        // Act
        String url = fileManagementService.generatePresignedDownloadUrl(metadata.getId(), false, 15);

        // Assert
        assertEquals("http://presigned-preview-url", url);
        verify(blobStorageManagerPort, times(1)).getPresignedDownloadUrl(eq(permanentBucket), anyString(), eq("test.pdf"), eq(false), eq(15));
    }
}
