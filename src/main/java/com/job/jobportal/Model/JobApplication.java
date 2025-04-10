package com.job.jobportal.Model;

import java.util.Date;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "resume")
public class JobApplication {
    public enum APPLICATIONSTATUS {
        NOTAPPLIED, APPLIED, SHORTLISTED, REJECTED
    }

    @Id
    private String id;
    private String firstname;
    private String lastname;
    private String jobId;
    private String useremail;
    private String filename;
    private String fileurl;
    private String filetype;
    private Long filesize;
    private Date uploadDate;
    private APPLICATIONSTATUS applicationStatus = APPLICATIONSTATUS.NOTAPPLIED;
}