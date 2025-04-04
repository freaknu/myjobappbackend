package com.job.jobportal.Controller;

import com.job.jobportal.Config.CustomUserDetailService;
import com.job.jobportal.Config.JwtUtils;
import com.job.jobportal.Dtos.*;
import com.job.jobportal.Model.JobApplication;
import com.job.jobportal.Model.JobInfo;
import com.job.jobportal.Model.UserInfo;
import com.job.jobportal.Service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/jobseeker")
@CrossOrigin(origins = "*")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private RedisService cache;
    @Autowired
    private UserService userservice;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private CustomUserDetailService userService;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private JobService jobservice;

    @Autowired
    private ResumeService resumeservice;

    @PostMapping("/auth/signup")
    public ResponseEntity<?> createUser(@RequestBody UserDto userDto) {
        try {
            if (userservice.isUserExists(userDto.useremail())) {
                return ResponseEntity.badRequest().body("User already exists with this email");
            }

            UserInfo newUser = new UserInfo();
            newUser.setUsername(userDto.username());
            newUser.setUseremail(userDto.useremail());
            newUser.setUserpassword(passwordEncoder.encode(userDto.userpassword()));
            newUser.setUserrole(userDto.role());

            userservice.save(newUser);
            cache.delete("users");

            return ResponseEntity.ok(new UserResponseDto(
                    newUser.getId(),
                    newUser.getUsername(),
                    newUser.getUseremail(),
                    newUser.getUserrole().name()));
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Problem while creating user");
        }
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> signin(@RequestParam String useremail, @RequestParam String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(useremail, password));
            UserDetails userDetails = userService.loadUserByUsername(useremail);
            String token = jwtUtils.generateToken(userDetails);
            UserInfo user = userservice.getuser(useremail);
            String role = user != null ? user.getUserrole().name() : "UNKNOWN";

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "role", role,
                    "email", useremail));
        } catch (BadCredentialsException e) {
            log.error("Invalid credentials: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        } catch (Exception e) {
            log.error("Error during login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during login");
        }
    }

    @GetMapping("/auth/profile")
    public ResponseEntity<?> getProfile() {
        try {
            String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
            UserInfo user = userservice.getuser(useremail);

            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(new UserResponseDto(
                    user.getId(),
                    user.getUsername(),
                    user.getUseremail(),
                    user.getUserrole().name()));
        } catch (Exception e) {
            log.error("Error fetching profile: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/profile/update")
    public ResponseEntity<?> updateProfile(@RequestBody UserDto userDto) {
        try {
            String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
            UserInfo user = userservice.getuser(useremail);

            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            if (userDto.username() != null) {
                user.setUsername(userDto.username());
            }
            if (userDto.userpassword() != null) {
                user.setUserpassword(passwordEncoder.encode(userDto.userpassword()));
            }

            userservice.save(user);
            cache.delete("users");

            return ResponseEntity.ok(new UserResponseDto(
                    user.getId(),
                    user.getUsername(),
                    user.getUseremail(),
                    user.getUserrole().name()));
        } catch (Exception e) {
            log.error("Error updating profile: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<JobResponseDto>> getAllJobs() {
        try {
            List<JobInfo> alljobs = jobservice.getAllJobs();

            List<JobResponseDto> response = alljobs.stream()
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

    @GetMapping("/jobs/search")
    public ResponseEntity<List<JobResponseDto>> searchJobs(
            @RequestParam(required = false) String technology,
            @RequestParam(required = false) String keyword) {
        try {
            List<JobInfo> jobs = jobservice.searchJobs(technology, keyword);

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
            log.error("Error searching jobs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/applyjob/{jobId}")
    public ResponseEntity<?> applyJob(@PathVariable String jobId, @RequestParam MultipartFile resume) {
        try {
            String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
            UserInfo user = userservice.getuser(useremail);
            JobInfo job = jobservice.getById(jobId);

            if (job == null) {
                return new ResponseEntity<>("Didn't found job", HttpStatus.CONFLICT);
            }

            if (user.getJobs().containsKey(jobId)) {
                return new ResponseEntity<>("Already applied for this job", HttpStatus.BAD_REQUEST);
            }

            if (resume == null || resume.isEmpty()) {
                return new ResponseEntity<>("Reusume Field is Required", HttpStatus.BAD_REQUEST);
            }
            JobApplication application = resumeservice.uploadResume(useremail, resume);

            Map<String, String> jobs = user.getJobs();
            jobs.put(jobId, useremail);
            user.setJobs(jobs);
            userservice.save(user);
            job.getApplicants().add(useremail);
            jobservice.saveJob(job);

            return new ResponseEntity<>("Application Submitted Successfully", HttpStatus.ACCEPTED);
        } catch (Exception e) {
            log.error("Error applying for job: {}", e.getMessage(), e);
            return new ResponseEntity<>("Error applying for job: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/myapplications")
    public ResponseEntity<?> getMyApplications() {
        try {
            String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
            UserInfo user = userservice.getuser(useremail);

            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            if (user.getJobs() == null || user.getJobs().isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            List<JobApplication> resumes = resumeservice.getUserApplications(useremail);

            List<JobApplicationUpload> applications = user.getJobs().keySet().stream()
                    .map(jobId -> {
                        try {
                            JobInfo job = jobservice.getById(jobId);
                            if (job == null) {
                                log.warn("Job not found with ID: {}", jobId);
                                return null;
                            }
                            JobApplication resume = resumes.stream()
                                    .filter(r -> jobId.equals(r.getJobId()))
                                    .findFirst()
                                    .orElse(null);

                            return new JobApplicationUpload(
                                    job.getJobid(),
                                    job.getJobname(),
                                    job.getJobposteruseremail(),
                                    String.join(",", job.getTechnology()),
                                    job.getJobdescription(),
                                    job.getJobpost(),
                                    resume);
                        } catch (JobService.ServiceException e) {
                            log.warn("Error fetching job {}: {}", jobId, e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            log.error("Error fetching applications: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching applications: " + e.getMessage());
        }
    }

    @DeleteMapping("/withdraw/{jobId}")
    public ResponseEntity<?> withdrawApplication(@PathVariable String jobId) {
        try {
            String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
            UserInfo user = userservice.getuser(useremail);
            JobInfo job = jobservice.getById(jobId);

            if (job == null) {
                return ResponseEntity.notFound().build();
            }

            if (!user.getJobs().containsKey(jobId)) {
                return ResponseEntity.badRequest().body("You haven't applied for this job");
            }
            user.getJobs().remove(jobId);
            userservice.save(user);
            job.getApplicants().remove(useremail);
            jobservice.saveJob(job);

            return ResponseEntity.ok(Map.of(
                    "message", "Application withdrawn successfully",
                    "jobId", jobId));
        } catch (Exception e) {
            log.error("Error withdrawing application: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error withdrawing application");
        }
    }
}