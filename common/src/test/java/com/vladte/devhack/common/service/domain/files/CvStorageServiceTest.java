package com.vladte.devhack.common.service.domain.files;

import com.vladte.devhack.common.config.TestConfig;
import com.vladte.devhack.common.service.BaseServiceTest;
import com.vladte.devhack.common.service.domain.files.impl.CvStorageServiceImpl;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the CvStorageService implementation.
 */
@Epic("Service Layer")
@Feature("CV Storage Management")
@Import(TestConfig.class)
public class CvStorageServiceTest extends BaseServiceTest {

    @Mock
    private MinioClient minioClient;

    private CvStorageService cvStorageService;
    private final String baseUrl = "http://localhost:9000";
    private final String bucketName = "resumes";

    @BeforeEach
    public void setup() {
        // Initialize the service with mocked dependencies
        cvStorageService = new CvStorageServiceImpl(minioClient, baseUrl);
    }

    @Test
    @DisplayName("Upload user CV should return file URL")
    @Description("Test that uploadUserCv successfully uploads a CV file and returns its URL")
    @Severity(SeverityLevel.CRITICAL)
    public void testUploadUserCv_Success() throws Exception {
        // Arrange
        String userId = "test-user-123";
        String fileName = "resume.pdf";
        String contentType = "application/pdf";
        byte[] fileContent = "Test CV content".getBytes();

        MockMultipartFile file = new MockMultipartFile(
                "file", fileName, contentType, fileContent);

        // Mock MinIO client behavior
        doNothing().when(minioClient).putObject(any(PutObjectArgs.class));

        // Act
        String result = cvStorageService.uploadUserCv(userId, file);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains(baseUrl));
        assertTrue(result.contains(bucketName));
        assertTrue(result.contains(userId));
        assertTrue(result.contains(fileName));

        // Verify MinIO client was called
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));

        // Attach test data to Allure report
        attachText("User ID", userId);
        attachText("File Name", fileName);
        attachText("Result URL", result);
    }

    @Test
    @DisplayName("Upload user CV with null file should throw exception")
    @Description("Test that uploadUserCv throws IllegalArgumentException when file is null")
    @Severity(SeverityLevel.NORMAL)
    public void testUploadUserCv_NullFile() throws Exception {
        // Arrange
        String userId = "test-user-123";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cvStorageService.uploadUserCv(userId, null)
        );

        assertTrue(exception.getMessage().contains("MultipartFile must not be null"));

        // Verify MinIO client was not called
        verify(minioClient, never()).putObject(any(PutObjectArgs.class));

        // Attach test data to Allure report
        attachText("User ID", userId);
        attachText("Exception Message", exception.getMessage());
    }

    @Test
    @DisplayName("Upload user CV with empty file should throw exception")
    @Description("Test that uploadUserCv throws IllegalArgumentException when file is empty")
    @Severity(SeverityLevel.NORMAL)
    public void testUploadUserCv_EmptyFile() throws Exception {
        // Arrange
        String userId = "test-user-123";
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cvStorageService.uploadUserCv(userId, emptyFile)
        );

        assertTrue(exception.getMessage().contains("MultipartFile must not be null or empty"));

        // Verify MinIO client was not called
        verify(minioClient, never()).putObject(any(PutObjectArgs.class));

        // Attach test data to Allure report
        attachText("User ID", userId);
        attachText("Exception Message", exception.getMessage());
    }

    @Test
    @DisplayName("Upload user CV with no filename should throw exception")
    @Description("Test that uploadUserCv throws IllegalArgumentException when file has no original filename")
    @Severity(SeverityLevel.NORMAL)
    public void testUploadUserCv_NoFilename() throws Exception {
        // Arrange
        String userId = "test-user-123";
        MockMultipartFile fileWithoutName = new MockMultipartFile(
                "file", null, "application/pdf", "content".getBytes());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cvStorageService.uploadUserCv(userId, fileWithoutName)
        );

        assertTrue(exception.getMessage().contains("must have an original filename"));

        // Verify MinIO client was not called
        verify(minioClient, never()).putObject(any(PutObjectArgs.class));

        // Attach test data to Allure report
        attachText("User ID", userId);
        attachText("Exception Message", exception.getMessage());
    }

    @Test
    @DisplayName("Upload user CV with MinIO exception should retry and eventually fail")
    @Description("Test that uploadUserCv retries on MinIO exceptions and eventually throws IOException")
    @Severity(SeverityLevel.NORMAL)
    public void testUploadUserCv_MinioException() throws Exception {
        // Arrange
        String userId = "test-user-123";
        String fileName = "resume.pdf";
        MockMultipartFile file = new MockMultipartFile(
                "file", fileName, "application/pdf", "content".getBytes());

        // Mock MinIO client to throw exception
        doThrow(new RuntimeException("MinIO connection failed"))
                .when(minioClient).putObject(any(PutObjectArgs.class));

        // Act & Assert
        IOException exception = assertThrows(
                IOException.class,
                () -> cvStorageService.uploadUserCv(userId, file)
        );

        assertTrue(exception.getMessage().contains("Operation failed after"));

        // Verify MinIO client was called multiple times (retry logic)
        verify(minioClient, times(3)).putObject(any(PutObjectArgs.class));

        // Attach test data to Allure report
        attachText("User ID", userId);
        attachText("File Name", fileName);
        attachText("Exception Message", exception.getMessage());
    }

    @Test
    @DisplayName("Download user CV should return input stream")
    @Description("Test that downloadUserCv successfully downloads a CV file and returns its input stream")
    @Severity(SeverityLevel.CRITICAL)
    public void testDownloadUserCv_Success() throws Exception {
        // Arrange
        String fileUrl = baseUrl + "/" + bucketName + "/test-user-123_resume.pdf";
        byte[] expectedContent = "Test CV content".getBytes();
        InputStream expectedStream = new ByteArrayInputStream(expectedContent);

        // Mock MinIO client behavior
        doReturn(expectedStream).when(minioClient).getObject(any(GetObjectArgs.class));

        // Act
        InputStream result = cvStorageService.downloadUserCv(fileUrl);

        // Assert
        assertNotNull(result);
        assertEquals(expectedStream, result);

        // Verify MinIO client was called
        verify(minioClient, times(1)).getObject(any(GetObjectArgs.class));

        // Attach test data to Allure report
        attachText("File URL", fileUrl);
    }

    @Test
    @DisplayName("Download user CV with invalid URL should throw exception")
    @Description("Test that downloadUserCv throws IllegalArgumentException when file URL is invalid")
    @Severity(SeverityLevel.NORMAL)
    public void testDownloadUserCv_InvalidUrl() throws Exception {
        // Arrange
        String invalidUrl = "http://invalid-url/invalid-bucket/file.pdf";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cvStorageService.downloadUserCv(invalidUrl)
        );

        assertTrue(exception.getMessage().contains("Invalid file URL"));

        // Verify MinIO client was not called
        verify(minioClient, never()).getObject(any(GetObjectArgs.class));

        // Attach test data to Allure report
        attachText("Invalid URL", invalidUrl);
        attachText("Exception Message", exception.getMessage());
    }

    @Test
    @DisplayName("Download user CV with MinIO exception should throw IOException")
    @Description("Test that downloadUserCv throws appropriate exception when MinIO fails")
    @Severity(SeverityLevel.NORMAL)
    public void testDownloadUserCv_MinioException() throws Exception {
        // Arrange
        String fileUrl = baseUrl + "/" + bucketName + "/test-user-123_resume.pdf";

        // Mock MinIO client to throw exception
        when(minioClient.getObject(any(GetObjectArgs.class)))
                .thenThrow(new MinioException("File not found"));

        // Act & Assert
        MinioException exception = assertThrows(
                MinioException.class,
                () -> cvStorageService.downloadUserCv(fileUrl)
        );

        assertEquals("File not found", exception.getMessage());

        // Verify MinIO client was called
        verify(minioClient, times(1)).getObject(any(GetObjectArgs.class));

        // Attach test data to Allure report
        attachText("File URL", fileUrl);
        attachText("Exception Message", exception.getMessage());
    }

    @Test
    @DisplayName("Upload user CV with different file types should work")
    @Description("Test that uploadUserCv works with different file types (PDF, DOCX, DOC)")
    @Severity(SeverityLevel.NORMAL)
    public void testUploadUserCv_DifferentFileTypes() throws Exception {
        // Arrange
        String userId = "test-user-123";
        String[] fileNames = {"resume.pdf", "resume.docx", "resume.doc"};
        String[] contentTypes = {"application/pdf",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/msword"};

        // Mock MinIO client behavior
        doNothing().when(minioClient).putObject(any(PutObjectArgs.class));

        for (int i = 0; i < fileNames.length; i++) {
            // Arrange for each file type
            MockMultipartFile file = new MockMultipartFile(
                    "file", fileNames[i], contentTypes[i], "content".getBytes());

            // Act
            String result = cvStorageService.uploadUserCv(userId, file);

            // Assert
            assertNotNull(result);
            assertTrue(result.contains(fileNames[i]));

            // Attach test data to Allure report
            attachText("File Type " + (i + 1), fileNames[i] + " - " + contentTypes[i]);
            attachText("Result URL " + (i + 1), result);
        }

        // Verify MinIO client was called for each file
        verify(minioClient, times(fileNames.length)).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("Upload user CV with large file should work")
    @Description("Test that uploadUserCv works with large files")
    @Severity(SeverityLevel.MINOR)
    public void testUploadUserCv_LargeFile() throws Exception {
        // Arrange
        String userId = "test-user-123";
        String fileName = "large-resume.pdf";
        // Create a 1MB file
        byte[] largeContent = new byte[1024 * 1024];
        MockMultipartFile largeFile = new MockMultipartFile(
                "file", fileName, "application/pdf", largeContent);

        // Mock MinIO client behavior
        doNothing().when(minioClient).putObject(any(PutObjectArgs.class));

        // Act
        String result = cvStorageService.uploadUserCv(userId, largeFile);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains(fileName));

        // Verify MinIO client was called
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));

        // Attach test data to Allure report
        attachText("User ID", userId);
        attachText("File Name", fileName);
        attachText("File Size", largeContent.length + " bytes");
        attachText("Result URL", result);
    }
}
