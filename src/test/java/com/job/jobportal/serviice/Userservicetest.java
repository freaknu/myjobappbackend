package com.job.jobportal.serviice;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.job.jobportal.Model.UserInfo;
import com.job.jobportal.Service.UserService;

@SpringBootTest
public class Userservicetest {

    @Autowired
    private UserService userService;

    @Test
    public void getAllUsers() {
        // List<UserInfo> users = userService.getalluser();
        // assertNotNull(users, "User list should not be null");
    }
}
