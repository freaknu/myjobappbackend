package com.job.jobportal.Repository;

import com.job.jobportal.Model.JobApplication;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends MongoRepository<JobApplication, String> {
    List<JobApplication> findByUseremail(String useremail);

    Optional<JobApplication> findByUseremailAndJobId(String email, String jobid);
}