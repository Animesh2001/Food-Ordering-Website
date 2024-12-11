package com.restaurant.food_ordering_website.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.List;

@Service
public class AWSService {

    @Autowired
    private AmazonS3 amazonS3Client;

//    @PostConstruct
//    public void init() {
//        listBuckets();
//    }
//    public List<Bucket> listBuckets(){
//        System.out.println("Inside buckets");
//        List<Bucket> bucketList =  amazonS3Client.listBuckets();
//        System.out.println(bucketList);
//        for(Bucket bucket: bucketList){
//            System.out.println("bucketName :" + bucket.getName());
//        }
//        return bucketList;
//    }

    public void uploadReport(String bucketName, ByteArrayInputStream reportStream, String reportName, long contentLength) {
        try {
            // Set metadata for the file (optional)
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(contentLength);  // size of the file
            metadata.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");  // MIME type for .xlsx files

            // Upload the file
            amazonS3Client.putObject(bucketName, reportName, reportStream, metadata);

            System.out.println("Report uploaded successfully: " + reportName);
        } catch (Exception e) {
            System.err.println("Error uploading report: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public S3Object getFileFromS3(String filePath) {
        try {
            // Split the file path to extract the bucket name and the key (file name)
            String[] pathParts = filePath.replace("s3://", "").split("/", 2);
            String bucketName = pathParts[0];
            String key = pathParts[1];

            // Get the file from S3
            S3Object s3Object = amazonS3Client.getObject(bucketName, key);

            System.out.println("File retrieved successfully: " + filePath);
            return s3Object;
        } catch (Exception e) {
            System.err.println("Error retrieving file: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
