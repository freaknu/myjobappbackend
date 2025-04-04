package com.job.jobportal.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.job.jobportal.Model.JobApplication;
import com.job.jobportal.Repository.ResumeRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ResumeService {
    @Autowired
    private ResumeRepository resumerepo;

    @Autowired
    private MongoTemplate mongtemplate;
    @Autowired(required = true)
    private AmazonS3 amazons3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public JobApplication uploadResume(String useremail, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String filename = generateFileName(useremail, file.getOriginalFilename());

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());
        amazons3.putObject(bucketName, filename, file.getInputStream(), metadata);

        String fileurl = amazons3.getUrl(bucketName, filename).toString();
        log.info("uploaded resume url is " + fileurl);
        JobApplication resume = new JobApplication();
        resume.setUseremail(useremail);
        resume.setFilename(file.getOriginalFilename());
        resume.setFileurl(fileurl);
        resume.setFiletype(file.getContentType());
        resume.setFilesize(file.getSize());
        resume.setUploadDate(new Date());

        return resumerepo.save(resume);
    }

    private String generateFileName(String userEmail, String originalFileName) {
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new IllegalArgumentException("Original file name cannot be empty");
        }

        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        return "resume/" + userEmail + "/" + UUID.randomUUID().toString() + fileExtension;
    }

    public List<JobApplication> getUserApplications(String useremail) {
        return resumerepo.findByUseremail(useremail);
    }

    public void deleteResume(String userEmail) {
        JobApplication resume = resumerepo.findByUseremail(userEmail).get(0);
        if (resume == null)
            return;
        String fileKey = resume.getFileurl().replaceFirst("https?://[^/]+/" + bucketName + "/", "");

        try {
            amazons3.deleteObject(bucketName, fileKey);
            resumerepo.delete(resume);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete resume from S3", e);
        }
    }

    public JobApplication getUserApplicationForJob(String useremail, String jobId) {
        Query query = new Query(
                Criteria.where("useremail").is(useremail)
                        .and("jobId").is(jobId));
        return mongtemplate.findOne(query, JobApplication.class);
    }

    public String generatePresignedUrlByUserEmail(String useremail) {
        // 1. Get the most recent resume for the user
        List<JobApplication> applications = resumerepo.findByUseremail(useremail);
        if (applications == null || applications.isEmpty()) {
            throw new IllegalArgumentException("No resume found for user: " + useremail);
        }

        JobApplication application = applications.stream()
                .sorted((a1, a2) -> a2.getUploadDate().compareTo(a1.getUploadDate()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No valid resume found"));

        String objectKey = extractS3KeyFromUrl(application.getFileurl());
        try {
            Date expiration = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));

            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectKey)
                    .withMethod(HttpMethod.GET)
                    .withExpiration(expiration);

            return amazons3.generatePresignedUrl(request).toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate pre-signed URL", e);
        }
    }

    private String extractS3KeyFromUrl(String fileUrl) {
        return fileUrl.replaceFirst("https?://[^/]+/" + bucketName + "/", "");
    }
}