package com.job.jobportal.Dtos;

import java.util.Date;

public record JobResponseDto(
        String jobid,
        String jobname,
        String jobposteruseremail,
        String technology,
        String jobdescription,
        Date jobpost) {
}