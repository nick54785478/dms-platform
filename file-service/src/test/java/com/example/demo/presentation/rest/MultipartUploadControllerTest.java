package com.example.demo.presentation.rest;

import com.example.demo.application.port.in.MultipartUploadUseCase;
import com.example.demo.application.shared.command.MultipartUploadCommand;
import com.example.demo.domain.file.aggregate.root.FileMetadata;
import com.example.demo.presentation.dto.CompleteMultipartRequest;
import com.example.demo.presentation.dto.InitiateMultipartRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MultipartUploadController.class)
class MultipartUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MultipartUploadUseCase multipartUploadUseCase;

    @Test
    void initiate_ShouldReturnInitiateMultipartResult() throws Exception {
        // Arrange
        InitiateMultipartRequest request = new InitiateMultipartRequest("VIDEO", "movie.mp4", "video/mp4", 5000L, "checksum", null);
        MultipartUploadUseCase.InitiateMultipartResult expectedResult = new MultipartUploadUseCase.InitiateMultipartResult("file-123", "upload-123");
        
        when(multipartUploadUseCase.initiateMultipartUpload(any(MultipartUploadCommand.InitiateCommand.class))).thenReturn(expectedResult);

        // Act & Assert
        mockMvc.perform(post("/api/v1/files/multipart/initiate")
                .header("X-Tenant-ID", "tenant-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileId").value("file-123"))
                .andExpect(jsonPath("$.uploadId").value("upload-123"));
    }

    @Test
    void getPresignedPartUrl_ShouldReturnUrlString() throws Exception {
        // Arrange
        String expectedUrl = "http://presigned-part";
        when(multipartUploadUseCase.getPresignedPartUrl(eq("file-123"), eq("upload-123"), eq(1), eq(20))).thenReturn(expectedUrl);

        // Act & Assert
        mockMvc.perform(get("/api/v1/files/multipart/{fileId}/{uploadId}/presigned-part", "file-123", "upload-123")
                .param("partNumber", "1")
                .param("expiryMinutes", "20"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedUrl));
    }

    @Test
    void complete_ShouldReturnFileResponse() throws Exception {
        // Arrange
        Map<Integer, String> etags = new HashMap<>();
        etags.put(1, "etag1");
        CompleteMultipartRequest request = new CompleteMultipartRequest(etags);
        
        FileMetadata metadata = FileMetadata.create("tenant-1", "movie.mp4", "VIDEO", "video/mp4", 5000L, null, null);
        when(multipartUploadUseCase.completeMultipartUpload(any(MultipartUploadCommand.CompleteCommand.class))).thenReturn(metadata);

        // Act & Assert
        mockMvc.perform(post("/api/v1/files/multipart/{fileId}/{uploadId}/complete", metadata.getId(), "upload-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(metadata.getId()))
                .andExpect(jsonPath("$.status").value("UNBOUND"));
    }

    @Test
    void abort_ShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(multipartUploadUseCase).abortMultipartUpload(any(MultipartUploadCommand.AbortCommand.class));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/files/multipart/{fileId}/{uploadId}/abort", "file-123", "upload-123"))
                .andExpect(status().isNoContent());
    }
}
