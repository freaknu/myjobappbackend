package com.job.jobportal.Controller;

import com.job.jobportal.Config.AwsS3Config;
import com.job.jobportal.Dtos.*;
import com.job.jobportal.Model.JobApplication;
import com.job.jobportal.Model.JobInfo;
import com.job.jobportal.Service.JobService;
import com.job.jobportal.Service.ResumeService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/jobprovider")
public class JobProviderController {

    @Autowired
    private JobService jobService;

    @Autowired
    private AwsS3Config awsservice;
    @Autowired
    private ResumeService resumeservice;

    @PostMapping("/addjob")
    public ResponseEntity<?> createJob(@RequestBody JobDto jobDto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication.getAuthorities().stream()
                    .noneMatch(a -> a.getAuthority().equals("JOBPROVIDER"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied. Requires JOBPROVIDER role");
            }

            String useremail = authentication.getName();
            List<String> technologies = Arrays.stream(jobDto.technology().split(","))
                    .map(String::trim)
                    .filter(tech -> !tech.isEmpty())
                    .collect(Collectors.toList());

            if (technologies.isEmpty()) {
                return ResponseEntity.badRequest().body("At least one technology is required");
            }

            JobInfo newJob = new JobInfo(
                    useremail,
                    jobDto.jobname(),
                    jobDto.jobdescription(),
                    technologies,
                    new Date());

            JobInfo savedJob = jobService.saveJob(newJob);

            JobResponseDto responseDto = new JobResponseDto(
                    savedJob.getJobid(),
                    savedJob.getJobname(),
                    savedJob.getJobposteruseremail(),
                    String.join(",", savedJob.getTechnology()),
                    savedJob.getJobdescription(),
                    savedJob.getJobpost());

            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            log.error("Error creating job: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating job: " + e.getMessage());
        }
    }

    @GetMapping("/myjobs")
    public ResponseEntity<List<JobResponseDto>> getMyJobs() {
        try {
            String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
            List<JobInfo> jobs = jobService.getJobsByPoster(useremail);

            List<JobResponseDto> response = jobs.stream()
                    .map(job -> new JobResponseDto(
                            job.getJobid(),
                            job.getJobname(),
                            job.getJobposteruseremail(),
                            String.join(",", job.getTechnology()),
                            job.getJobdescription(),
                            job.getJobpost()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching jobs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/view-resume/{useremail}")
    public ResponseEntity<Map<String, Object>> viewResume(
            @PathVariable String useremail) {
        try {
            JobApplication application = resumeservice.getUserApplications(useremail).get(0);
            if (application == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No resume found"));
            }
            log.info(application.getFileurl());
            Map<String, Object> response = new HashMap<>();
            response.put("useremail", application.getUseremail());
            response.put("filename", application.getFilename());
            response.put("fileType", application.getFiletype());
            response.put("resumerl", application.getFileurl());
            response.put("fileSize", application.getFilesize());
            response.put("uploadDate", application.getUploadDate());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch resume"));
        }
    }

    @GetMapping("/job/getapplicants/{id}")
    public ResponseEntity<List<UserResponseDto>> getJobApplicants(@PathVariable String id) {
        try {
            JobInfo job = jobService.getById(id);
            if (job == null) {
                return ResponseEntity.notFound().build();
            }
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            if (!job.getJobposteruseremail().equals(currentUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<UserResponseDto> applicants = jobService.getJobApplicants(id);
            return ResponseEntity.ok(applicants);
        } catch (Exception e) {
            log.error("Error fetching applicants: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/deletejob/{jobId}")
    public ResponseEntity<?> deleteJob(@PathVariable("jobId") String jobId) {
        try {
            if (jobId == null || jobId.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Job ID cannot be empty");
            }

            log.info("Attempting to delete job with ID: {}", jobId);

            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

            JobInfo job = jobService.getById(jobId);
            if (job == null) {
                log.warn("Job not found with ID: {}", jobId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Job not found with ID: " + jobId);
            }

            if (!currentUserEmail.equals(job.getJobposteruseremail())) {
                log.warn("Unauthorized delete attempt by {} for job {}", currentUserEmail, jobId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not authorized to delete this job");
            }

            jobService.deleteById(jobId);
            log.info("Successfully deleted job with ID: {}", jobId);

            return ResponseEntity.ok().body("Job deleted successfully");

        } catch (Exception e) {
            log.error("Error deleting job with ID {}: {}", jobId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while deleting the job");
        }
    }
}