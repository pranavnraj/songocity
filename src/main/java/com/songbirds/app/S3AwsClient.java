package com.songbirds.app;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.songbirds.util.AWSClientConstants;

import java.io.File;

public class S3AwsClient {

    private static S3AwsClient s3AwsClient= new S3AwsClient();
    private Regions clientRegion = Regions.US_WEST_1;
    private String bucketName = AWSClientConstants.DATA_BUCKET;
    private AmazonS3 s3Client;


    private S3AwsClient() {
        s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(clientRegion)
                .build();

    }

    public static S3AwsClient getInstance() {return s3AwsClient;}

    public void putFileInS3(String keyName, String fileName) {
        String [] split = fileName.split("/");
        String name = split[split.length - 1];
        s3Client.putObject(this.bucketName, keyName + name, new File(fileName));
    }



}
