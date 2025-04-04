package com.job.jobportal.Service;

import com.job.jobportal.Dtos.UserResponseDto;
import com.job.jobportal.Model.JobInfo;
import com.job.jobportal.Model.UserInfo;
import com.job.jobportal.Repository.JobinfoRepository;
import com.job.jobportal.Repository.UserInfoRepository;
import org.springframework.util.StringUtils;

import org.springframework.stereotype.Service;

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
        try {
            return jobRepo.save(jobData);
        } catch (Exception e) {
            throw new ServiceException("Failed to save job", e);
        }
    }

    public List<JobInfo> getAllJobs() {
        try {
            return jobRepo.findAll();
        } catch (Exception e) {
            throw new ServiceException("Failed to fetch all jobs", e);
        }
    }

    public JobInfo getById(String jobId) {
        Objects.requireNonNull(jobId, "Job ID cannot be null");
        try {
            return jobRepo.findById(jobId)
                    .orElseThrow(() -> new NotFoundException("Job not found with id: " + jobId));
        } catch (Exception e) {
            throw new ServiceException("Failed to fetch job by id: " + jobId, e);
        }
    }

    public void deleteById(String id) {
        Objects.requireNonNull(id, "Job ID cannot be null");
        try {
            if (!jobRepo.existsById(id)) {
                throw new NotFoundException("Job not found with id: " + id);
            }
            jobRepo.deleteById(id);
        } catch (Exception e) {
            throw new ServiceException("Failed to delete job: " + id, e);
        }
    }

    public UserInfo getUser(String email) {
        Objects.requireNonNull(email, "Email cannot be null");
        try {
            return userRepo.findByUseremail(email)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        } catch (Exception e) {
            throw new ServiceException("Failed to fetch user by email: " + email, e);
        }
    }

    public List<UserResponseDto> getJobApplicants(String jobId) {
        Objects.requireNonNull(jobId, "Job ID cannot be null");
        try {
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
        } catch (Exception e) {
            throw new ServiceException("Failed to fetch job applicants for job: " + jobId, e);
        }
    }

    public List<JobInfo> getJobsByPoster(String userEmail) {
        Objects.requireNonNull(userEmail, "User email cannot be null");
        try {
            return jobRepo.findByJobposteruseremail(userEmail);
        } catch (Exception e) {
            throw new ServiceException("Failed to fetch jobs by poster: " + userEmail, e);
        }
    }

    public List<JobInfo> searchJobs(String technology, String keyword) {
        try {
            boolean hasTechnology = StringUtils.hasText(technology);
            boolean hasKeyword = StringUtils.hasText(keyword);

            if (hasTechnology && hasKeyword) {
                if (technology.contains(",")) {
                    List<String> techList = java.util.Arrays.asList(technology.split("\\s*,\\s*"));
                    return jobRepo.findByTechnologiesInAndJobnameContainingIgnoreCase(techList, keyword);
                }
                return jobRepo.findByTechnologyAndJobnameContainingIgnoreCase(technology, keyword);
            } else if (hasTechnology) {
                if (technology.contains(",")) {
                    List<String> techList = java.util.Arrays.asList(technology.split("\\s*,\\s*"));
                    return jobRepo.findByTechnologiesIn(techList);
                }
                return jobRepo.findByTechnologyContainingIgnoreCase(technology);
            } else if (hasKeyword) {
                return jobRepo.findByJobnameContainingIgnoreCase(keyword);
            }

            return jobRepo.findRecentJobs();
        } catch (Exception e) {
            throw new ServiceException("Failed to search jobs", e);
        }
    }

    public static class ServiceException extends RuntimeException {
        public ServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }
}