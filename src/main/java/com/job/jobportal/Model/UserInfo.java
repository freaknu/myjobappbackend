package com.job.jobportal.Model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.*;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "userinfo")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {
    public enum Role {
        JOBSEEKER,
        JOBPROVIDER
    }

    @Id
    private String id;

    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String useremail;

    @NotBlank
    @Size(min = 8)
    private String userpassword;

    @NotNull
    private Role userrole = Role.JOBSEEKER;
    private Map<String, String> jobs = new HashMap<>();
}