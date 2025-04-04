package com.job.jobportal.Model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "resume")
public class JobApplication {
    @Id
    private String Id;
    private String jobId;
    private String useremail;
    private String Filename;
    private String Fileurl;
    private String Filetype;
    private Long Filesize;
    private Date UploadDate;
}