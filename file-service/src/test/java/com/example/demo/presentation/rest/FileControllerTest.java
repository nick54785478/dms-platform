package com.example.demo.presentation.rest;

import com.example.demo.application.port.in.ManageFileUseCase;
import com.example.demo.application.shared.command.PresignedUrlCommand;
import com.example.demo.application.shared.command.UploadFileCommand;
import com.example.demo.domain.file.aggregate.root.FileMetadata;
import com.example.demo.presentation.dto.PresignedUploadRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ManageFileUseCase manageFileUseCase;

    @Test
    void uploadFile_ShouldReturnFileResponse_WhenUploadIsSuccessful() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "content".getBytes());
        FileMetadata metadata = FileMetadata.create("tenant-1", "test.txt", "DOCUMENT", MediaType.TEXT_PLAIN_VALUE, 7L, null, null);
        
        when(manageFileUseCase.uploadFile(any(UploadFileCommand.class))).thenReturn(metadata);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/files/upload")
                .file(file)
                .header("X-Tenant-ID", "tenant-1")
                .param("type", "DOCUMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(metadata.getId()))
                .andExpect(jsonPath("$.originalFileName").value("test.txt"))
                .andExpect(jsonPath("$.tenantId").value("tenant-1"))
                .andExpect(jsonPath("$.status").value("UNBOUND"));
    }

    @Test
    void getPresignedUploadUrl_ShouldReturnUrlResult() throws Exception {
        // Arrange
        PresignedUploadRequest request = new PresignedUploadRequest("DOCUMENT", "video.mp4", "video/mp4", 1024L, null, null, 15);
        ManageFileUseCase.PresignedUrlResult expectedResult = new ManageFileUseCase.PresignedUrlResult("uuid-123", "http://presigned-url");
        
        when(manageFileUseCase.generatePresignedUploadUrl(any(PresignedUrlCommand.class))).thenReturn(expectedResult);

        // Act & Assert
        mockMvc.perform(post("/api/v1/files/presigned-upload")
                .header("X-Tenant-ID", "tenant-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileId").value("uuid-123"))
                .andExpect(jsonPath("$.presignedUrl").value("http://presigned-url"));
    }

    @Test
    void getPresignedDownloadUrl_ShouldReturnUrlString() throws Exception {
        // Arrange
        String fileId = "uuid-123";
        String expectedUrl = "http://presigned-download-url";
        
        when(manageFileUseCase.generatePresignedDownloadUrl(eq(fileId), anyInt())).thenReturn(expectedUrl);

        // Act & Assert
        mockMvc.perform(get("/api/v1/files/{fileId}/presigned-download", fileId)
                .param("expiryMinutes", "30"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedUrl));
    }
}
