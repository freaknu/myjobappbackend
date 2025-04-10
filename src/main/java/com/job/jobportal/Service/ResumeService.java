package com.job.jobportal.Service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.job.jobportal.Model.JobApplication;
import com.job.jobportal.Model.JobApplication.APPLICATIONSTATUS;
import com.job.jobportal.Repository.ResumeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ResumeService {
    private final ResumeRepository resumeRepo;
    private final MongoTemplate mongoTemplate;
    private final AmazonS3 amazonS3;
    private final String bucketName;

    public ResumeService(ResumeRepository resumeRepo, MongoTemplate mongoTemplate,
            AmazonS3 amazonS3, @Value("${cloud.aws.s3.bucket}") String bucketName) {
        this.resumeRepo = resumeRepo;
        this.mongoTemplate = mongoTemplate;
        this.amazonS3 = amazonS3;
        this.bucketName = bucketName;
    }

    public JobApplication uploadResume(String firstname, String lastname, String jobid,
            String useremail, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String filename = generateFileName(useremail, file.getOriginalFilename());

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());
        amazonS3.putObject(bucketName, filename, file.getInputStream(), metadata);

        String fileurl = amazonS3.getUrl(bucketName, filename).toString();

        JobApplication resume = new JobApplication();
        resume.setUseremail(useremail);
        resume.setFilename(file.getOriginalFilename());
        resume.setFileurl(fileurl);
        resume.setFiletype(file.getContentType());
        resume.setFilesize(file.getSize());
        resume.setUploadDate(new Date());
        resume.setFirstname(firstname);
        resume.setJobId(jobid);
        resume.setLastname(lastname);
        resume.setApplicationStatus(APPLICATIONSTATUS.APPLIED);

        return resumeRepo.save(resume);
    }

    private String generateFileName(String userEmail, String originalFileName) {
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new IllegalArgumentException("Original file name cannot be empty");
        }
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        return "resume/" + userEmail + "/" + UUID.randomUUID() + fileExtension;
    }

    public List<JobApplication> getUserApplications(String useremail) {
        return resumeRepo.findByUseremail(useremail);
    }

    public JobApplication getUserApplicationForJob(String useremail, String jobId) {
        Query query = new Query(
                Criteria.where("useremail").is(useremail)
                        .and("jobId").is(jobId));
        return mongoTemplate.findOne(query, JobApplication.class);
    }

    public String generatePresignedUrlByUserEmail(String useremail) {
        List<JobApplication> applications = resumeRepo.findByUseremail(useremail);
        if (applications == null || applications.isEmpty()) {
            throw new IllegalArgumentException("No resume found for user: " + useremail);
        }

        JobApplication application = applications.stream()
                .max((a1, a2) -> a2.getUploadDate().compareTo(a1.getUploadDate()))
                .orElseThrow(() -> new IllegalArgumentException("No valid resume found"));

        String objectKey = extractS3KeyFromUrl(application.getFileurl());
        Date expiration = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectKey)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);

        return amazonS3.generatePresignedUrl(request).toString();
    }

    public void save(JobApplication job) {
        resumeRepo.save(job);
    }

    private String extractS3KeyFromUrl(String fileUrl) {
        return fileUrl.replaceFirst("https?://[^/]+/" + bucketName + "/", "");
    }
}