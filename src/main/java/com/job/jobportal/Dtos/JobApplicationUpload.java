package com.job.jobportal.Dtos;

import java.util.Date;

import com.job.jobportal.Model.JobApplication;
import com.job.jobportal.Model.JobApplication.APPLICATIONSTATUS;

public record JobApplicationUpload(
        String jobid,
        String jobname,
        String jobposteruseremail,
        String technology,
        String jobdescription,
        Date jobpost,
        JobApplication resume) {
}