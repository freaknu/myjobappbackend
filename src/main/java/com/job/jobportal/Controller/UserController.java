package com.job.jobportal.Controller;

import com.job.jobportal.Config.JwtUtils;
import com.job.jobportal.Dtos.*;
import com.job.jobportal.Model.JobApplication;
import com.job.jobportal.Model.JobInfo;
import com.job.jobportal.Model.UserInfo;
import com.job.jobportal.Service.*;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@RestController
@RequestMapping("/jobseeker")
@CrossOrigin(origins = "*")
public class UserController {
    private final UserService userservice;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final JobService jobService;
    private final ResumeService resumeService;
    private final RedisService redisService;
    @Autowired
    private UserDetailsService userdetails;

    public UserController(UserService userService, PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager, JwtUtils jwtUtils,
            JobService jobService, ResumeService resumeService,
            RedisService redisService) {
        this.userservice = userService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.jobService = jobService;
        this.resumeService = resumeService;
        this.redisService = redisService;
    }

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
            redisService.delete("users");

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
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userdetail = userdetails.loadUserByUsername(useremail);
            String token = jwtUtils.generateToken(userdetail);
            UserInfo user = userservice.getUser(useremail);
            String role = user.getUserrole().name();

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "role", role,
                    "email", useremail));
        } catch (Exception e) {
            log.error("Error during login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }


    @GetMapping("/jobs")
    public ResponseEntity<List<JobResponseDto>> getAllJobs() {
        try {
            List<JobInfo> alljobs = jobService.getAllJobs();
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

    @GetMapping("jobs/search")
    public ResponseEntity<List<JobResponseDto>> searchJobs(
            @RequestParam(required = false) String technology,
            @RequestParam(required = false) String keyword) {
        try {
            List<JobInfo> jobs = jobService.searchJobs(technology, keyword);
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
    public ResponseEntity<?> applyJob(@PathVariable String jobId,
            @RequestParam("resume") MultipartFile resume,
            @RequestPart("applydetails") ApplyDTO applydetails) {
        try {
            String firstname = applydetails.firstname();
            String lastname = applydetails.lastname();
            String useremail = applydetails.email() != null ? applydetails.email()
                    : SecurityContextHolder.getContext().getAuthentication().getName();

            UserInfo user = userservice.getUser(useremail);
            JobInfo job = jobService.getById(jobId);

            if (job == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Job not found");
            }

            if (user.getJobs().containsKey(jobId)) {
                return ResponseEntity.badRequest().body("Already applied for this job");
            }

            if (resume == null || resume.isEmpty()) {
                return ResponseEntity.badRequest().body("Resume is required");
            }

            resumeService.uploadResume(firstname, lastname, jobId, useremail, resume);
            Map<String, String> jobs = user.getJobs();
            jobs.put(jobId, useremail);
            user.setJobs(jobs);
            userservice.save(user);

            job.getApplicants().add(useremail);
            jobService.saveJob(job);

            return ResponseEntity.ok("Job applied successfully");
        } catch (Exception e) {
            log.error("Error applying for job: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error applying for job: " + e.getMessage());
        }
    }

    @GetMapping("/myapplications")
    public ResponseEntity<?> getMyApplications() {
        try {
            String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
            UserInfo user = userservice.getUser(useremail);

            if (user.getJobs() == null || user.getJobs().isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            List<JobApplication> resumes = resumeService.getUserApplications(useremail);

            List<JobApplicationUpload> applications = user.getJobs().keySet().stream()
                    .map(jobId -> {
                        try {
                            JobInfo job = jobService.getById(jobId);
                            if (job == null)
                                return null;

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
                        } catch (Exception e) {
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
            UserInfo user = userservice.getUser(useremail);
            JobInfo job = jobService.getById(jobId);

            if (job == null) {
                return ResponseEntity.notFound().build();
            }

            if (!user.getJobs().containsKey(jobId)) {
                return ResponseEntity.badRequest().body("You haven't applied for this job");
            }

            Map<String, String> jobs = user.getJobs();
            jobs.remove(jobId);
            user.setJobs(jobs);
            userservice.save(user);

            List<String> applicants = job.getApplicants();
            applicants.remove(useremail);
            job.setApplicants(applicants);
            jobService.saveJob(job);

            return ResponseEntity.ok(Map.of(
                    "message", "Application withdrawn successfully",
                    "jobId", jobId));
        } catch (Exception e) {
            log.error("Error withdrawing application: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error withdrawing application");
        }
    }

    @GetMapping("/get-jobapplication/{jobid}")
    public ResponseEntity<?> getApplication(@PathVariable String jobid) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            JobApplication job = resumeService.getUserApplicationForJob(email, jobid);
            if (job == null) {
                return new ResponseEntity<>("Job Didn't Found", HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(job, HttpStatus.OK);
        } catch (Exception e) {
            throw e;
        }
    }

}