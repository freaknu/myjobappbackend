package com.job.jobportal.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "jobs")
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class JobInfo {
    @Id
    private String jobid;
    private String jobposteruseremail;
    private String jobname;
    private String jobdescription;
    private List<String> technology = new ArrayList<>();
    private Date jobpost;
    private List<String> applicants = new ArrayList<>();

    public JobInfo(String useremail, String jobname, String jobdescription,
            List<String> technology, Date jobpost) {
        this.jobposteruseremail = useremail;
        this.jobname = jobname;
        this.jobdescription = jobdescription;
        this.technology = technology;
        this.jobpost = jobpost;
    }
}