package com.example.demo.application.service;

import com.example.demo.application.port.out.BlobStorageManagerPort;
import com.example.demo.application.port.out.DistributedLockerPort;
import com.example.demo.application.port.out.FileMetadataRepositoryPort;
import com.example.demo.application.shared.command.FileBoundCommand;
import com.example.demo.application.shared.command.FileDeletedCommand;
import com.example.demo.domain.file.aggregate.root.FileMetadata;
import com.example.demo.domain.file.aggregate.vo.FileStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileEventServiceTest {

    @Mock
    private FileMetadataRepositoryPort fileMetadataRepositoryPort;

    @Mock
    private BlobStorageManagerPort blobStorageManagerPort;

    @Mock
    private DistributedLockerPort distributedLockerPort;

    private FileEventService fileEventService;

    private final String tempBucket = "temp-bucket";
    private final String permanentBucket = "permanent-bucket";

    @BeforeEach
    void setUp() {
        fileEventService = new FileEventService(
                fileMetadataRepositoryPort,
                blobStorageManagerPort,
                distributedLockerPort,
                tempBucket,
                permanentBucket
        );

        // Mock the lock to immediately execute the lambda
        lenient().doAnswer(invocation -> {
            Runnable task = invocation.getArgument(3);
            task.run();
            return null;
        }).when(distributedLockerPort).runWithWatchdog(anyString(), anyLong(), any(TimeUnit.class), any(Runnable.class));
    }

    @Test
    void handleFileBoundEvent_WhenFileIsUnbound_ShouldMoveToPermanentAndMarkBound() throws Exception {
        // Arrange
        String fileId = "file-123";
        String tenantId = "tenant-A";
        FileBoundCommand command = new FileBoundCommand(fileId, tenantId);

        FileMetadata metadata = FileMetadata.create(tenantId, "test.pdf", "DOCUMENT", "application/pdf", 1024L, "checksum", null);
        // Using reflection or assuming it's UNBOUND by default.
        assertEquals(FileStatus.UNBOUND, metadata.getStatus());
        
        // Mocking the repository to return the metadata
        // Note: we can't easily set fileId on the new metadata because ID is generated randomly in create().
        // For testing, let's just mock findById to return this metadata.
        when(fileMetadataRepositoryPort.findById(fileId)).thenReturn(Optional.of(metadata));

        // Act
        fileEventService.handleFileBoundEvent(command);

        // Assert
        assertEquals(FileStatus.BOUND, metadata.getStatus());
        
        // Verify clone and delete were called
        verify(blobStorageManagerPort, times(1)).cloneFile(eq(tempBucket), anyString(), eq(permanentBucket), anyString());
        verify(blobStorageManagerPort, times(1)).deleteFile(eq(tempBucket), eq(""), anyString());
        
        // Verify save was called
        verify(fileMetadataRepositoryPort, times(1)).save(metadata);
    }

    @Test
    void handleFileBoundEvent_WhenFileIsAlreadyBound_ShouldBeIdempotentAndDoNothing() throws Exception {
        // Arrange
        String fileId = "file-123";
        FileBoundCommand command = new FileBoundCommand(fileId, "tenant-A");

        FileMetadata metadata = FileMetadata.create("tenant-A", "test.pdf", "DOCUMENT", "application/pdf", 1024L, "checksum", null);
        metadata.markAsBound(); // Set to BOUND initially

        when(fileMetadataRepositoryPort.findById(fileId)).thenReturn(Optional.of(metadata));

        // Act
        fileEventService.handleFileBoundEvent(command);

        // Assert
        // Should return early, no external calls
        verify(blobStorageManagerPort, never()).cloneFile(anyString(), anyString(), anyString(), anyString());
        verify(fileMetadataRepositoryPort, never()).save(any(FileMetadata.class));
    }

    @Test
    void handleFileDeletedEvent_WhenFileIsBound_ShouldDeleteFromPermanentAndMarkDeleted() throws Exception {
        // Arrange
        String fileId = "file-123";
        FileDeletedCommand command = new FileDeletedCommand(fileId, "tenant-A");

        FileMetadata metadata = FileMetadata.create("tenant-A", "test.pdf", "DOCUMENT", "application/pdf", 1024L, "checksum", null);
        metadata.markAsBound();

        when(fileMetadataRepositoryPort.findById(fileId)).thenReturn(Optional.of(metadata));

        // Act
        fileEventService.handleFileDeletedEvent(command);

        // Assert
        assertEquals(FileStatus.DELETED, metadata.getStatus());
        
        // Verify delete was called on permanent bucket
        verify(blobStorageManagerPort, times(1)).deleteFile(eq(permanentBucket), eq(""), anyString());
        verify(fileMetadataRepositoryPort, times(1)).save(metadata);
    }
}
