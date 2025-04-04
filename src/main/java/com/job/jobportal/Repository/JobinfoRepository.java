package com.job.jobportal.Repository;

import com.job.jobportal.Model.JobInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;

public interface JobinfoRepository extends MongoRepository<JobInfo, String> {

    @Query("{ 'jobid' : ?0 }")
    JobInfo findByJobid(String jobid);

    @Query("{ 'jobposteruseremail' : ?0 }")
    List<JobInfo> findByJobposteruseremail(String useremail);

    @Query("""
            { $and: [
                { 'technology': { $regex: ?0, $options: 'i' } },
                { 'jobname': { $regex: ?1, $options: 'i' } }
            ]}
            """)
    List<JobInfo> findByTechnologyAndJobnameContainingIgnoreCase(String technology, String keyword);

    @Query("{ 'technology': { $regex: ?0, $options: 'i' } }")
    List<JobInfo> findByTechnologyContainingIgnoreCase(String technology);

    @Query("{ 'technology': { $in: ?0 } }")
    List<JobInfo> findByTechnologiesIn(List<String> technologies);

    @Query("""
            { $and: [
                { 'technology': { $in: ?0 } },
                { 'jobname': { $regex: ?1, $options: 'i' } }
            ]}
            """)
    List<JobInfo> findByTechnologiesInAndJobnameContainingIgnoreCase(List<String> technologies, String keyword);

    @Query("{ 'jobname': { $regex: ?0, $options: 'i' } }")
    List<JobInfo> findByJobnameContainingIgnoreCase(String keyword);

    @Query(value = "{}", sort = "{ 'jobpost': -1 }")
    List<JobInfo> findRecentJobs();

    long countByJobposteruseremail(String useremail);

    List<JobInfo> findByJobpostAfter(Date date);
}