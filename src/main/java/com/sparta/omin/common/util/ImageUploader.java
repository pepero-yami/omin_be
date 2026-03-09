package com.sparta.omin.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class ImageUploader {
    private final S3Service s3Service;

    public String uploadReviewImage(MultipartFile file) {
        return s3Service.uploadImage(file);
    }

    public void deleteReviewImage(String url) { // S3에서 해당 url을 가진 이미지 삭제
    }
}
