package com.job.jobportal.Service;

import com.job.jobportal.Dtos.UserResponseDto;
import com.job.jobportal.Model.JobInfo;
import com.job.jobportal.Model.UserInfo;
import com.job.jobportal.Repository.JobinfoRepository;
import com.job.jobportal.Repository.UserInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class JobService {
    private final JobinfoRepository jobRepo;
    private final UserInfoRepository userRepo;

    public JobService(JobinfoRepository jobRepo, UserInfoRepository userRepo) {
        this.jobRepo = jobRepo;
        this.userRepo = userRepo;
    }

    public JobInfo saveJob(JobInfo jobData) {
        Objects.requireNonNull(jobData, "Job data cannot be null");
        return jobRepo.save(jobData);
    }

    public List<JobInfo> getAllJobs() {
        return jobRepo.findAll();
    }

    public JobInfo getById(String jobId) {
        Objects.requireNonNull(jobId, "Job ID cannot be null");
        return jobRepo.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Job not found with id: " + jobId));
    }

    public void deleteById(String id) {
        Objects.requireNonNull(id, "Job ID cannot be null");
        if (!jobRepo.existsById(id)) {
            throw new NotFoundException("Job not found with id: " + id);
        }
        jobRepo.deleteById(id);
    }

    public UserInfo getUser(String email) {
        Objects.requireNonNull(email, "Email cannot be null");
        return userRepo.findByUseremail(email)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }

    public List<UserResponseDto> getJobApplicants(String jobId) {
        Objects.requireNonNull(jobId, "Job ID cannot be null");
        JobInfo job = getById(jobId);
        return job.getApplicants().stream()
                .map(email -> userRepo.findByUseremail(email))
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0))
                .map(user -> new UserResponseDto(
                        user.getId(),
                        user.getUsername(),
                        user.getUseremail(),
                        user.getUserrole().name()))
                .collect(Collectors.toList());
    }

    public List<JobInfo> getJobsByPoster(String userEmail) {
        Objects.requireNonNull(userEmail, "User email cannot be null");
        return jobRepo.findByJobposteruseremail(userEmail);
    }

    public List<JobInfo> searchJobs(String technology, String keyword) {
        boolean hasTechnology = StringUtils.hasText(technology);
        boolean hasKeyword = StringUtils.hasText(keyword);

        if (hasTechnology && hasKeyword) {
            if (technology.contains(",")) {
                List<String> techList = List.of(technology.split("\\s*,\\s*"));
                return jobRepo.findByTechnologiesIn(techList).stream()
                        .filter(job -> job.getJobname().toLowerCase().contains(keyword.toLowerCase()))
                        .collect(Collectors.toList());
            }
            return jobRepo.findByTechnologyContainingIgnoreCase(technology).stream()
                    .filter(job -> job.getJobname().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
        } else if (hasTechnology) {
            if (technology.contains(",")) {
                List<String> techList = List.of(technology.split("\\s*,\\s*"));
                return jobRepo.findByTechnologiesIn(techList);
            }
            return jobRepo.findByTechnologyContainingIgnoreCase(technology);
        } else if (hasKeyword) {
            return jobRepo.findByJobnameContainingIgnoreCase(keyword);
        }
        return jobRepo.findRecentJobs();
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }
}