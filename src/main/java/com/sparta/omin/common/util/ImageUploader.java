package com.sparta.omin.common.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ImageUploader {
    public String uploadReviewImage (MultipartFile file){
        return "업로드 이후 생성된 S3 url";
    }

    public void deleteReviewImage(String url) { // S3에서 해당 url을 가진 이미지 삭제
    }
}
