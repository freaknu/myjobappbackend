package com.job.jobportal.Dtos;

import java.util.Date;

import com.job.jobportal.Model.JobApplication;

public record JobApplicationUpload(String id,
        String jobname,
        String jobposteruseremail,
        String technology,
        String jobdescription,
        Date postedDate, JobApplication resume) {

}
