package com.vladte.devhack.domain.service.files;

import io.minio.errors.MinioException;
import lombok.SneakyThrows;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface CvStorageService {
    @SneakyThrows({IOException.class, MinioException.class, NoSuchAlgorithmException.class, InvalidKeyException.class})
    String uploadUserCv(String userId, MultipartFile file) throws IOException;

    InputStream downloadUserCv(String fileUrl) throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException;
}
