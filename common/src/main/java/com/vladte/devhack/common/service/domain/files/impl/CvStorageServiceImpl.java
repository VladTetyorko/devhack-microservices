package com.vladte.devhack.common.service.domain.files.impl;

import com.vladte.devhack.common.service.domain.files.CvStorageService;
import com.vladte.devhack.common.service.domain.files.FileStorageService;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class CvStorageServiceImpl extends FileStorageService implements CvStorageService {

    public CvStorageServiceImpl(MinioClient minioClient, @Value("${minio.url}") String baseUrl) {
        super(minioClient, baseUrl);
    }

    @Override
    protected String getBucketName() {
        return "resumes";
    }

    /**
     * Uploads a user CV to MinIO and returns its accessible URL.
     *
     * @param userId identifier of the user, used to organize objects
     * @param file   multipart file representing the CV
     * @return public URL to access the uploaded CV
     * @throws IOException on file read failure
     */
    @Override
    public String uploadUserCv(String userId, MultipartFile file) throws IOException {
        return uploadFile(userId, file);
    }

    /**
     * Retrieves a user CV InputStream from MinIO based on its public URL.
     */
    @Override
    public InputStream downloadUserCv(String fileUrl) throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException {
        return downloadFile(fileUrl);
    }
}
