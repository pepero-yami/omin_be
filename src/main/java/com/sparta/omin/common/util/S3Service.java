package com.sparta.omin.common.util;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    public String uploadImage(MultipartFile file) {
        // 1. S3에서 파일 이름이 겹치지 않게 UUID를 붙여서 생성합니다.
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        // 2. S3에 업로드
        try {
            var result = s3Template.upload(bucketName, fileName, file.getInputStream());
            // 3. 업로드된 파일의 공개 URL을 반환합니다.
            return result.getURL().toString();
        } catch (IOException e) {
            throw new RuntimeException("S3 파일 업로드 중 오류가 발생했습니다.", e);
        }
    }
}