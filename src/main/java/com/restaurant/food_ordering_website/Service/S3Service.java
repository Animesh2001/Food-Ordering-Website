package com.restaurant.food_ordering_website.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Service
public class S3Service {

//    @Autowired
//    private AmazonS3 s3Client;

//    @Value("${aws.s3.bucket-name}")
//    private String bucketName;

    public void uploadReport(String reportName, ByteArrayOutputStream reportContent) throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(reportContent.toByteArray());
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(reportContent.size());
        metadata.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

//        s3Client.putObject(bucketName, reportName, inputStream, metadata);
    }
}
