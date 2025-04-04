package com.job.jobportal.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.job.jobportal.Model.JobApplication;

@Repository
public interface ResumeRepository extends MongoRepository<JobApplication, String> {

    List<JobApplication> findByUseremail(String useremail);
}
