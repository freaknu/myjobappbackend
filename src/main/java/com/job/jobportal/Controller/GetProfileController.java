package com.job.jobportal.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.job.jobportal.Dtos.UserDto;
import com.job.jobportal.Dtos.UserResponseDto;
import com.job.jobportal.Model.UserInfo;
import com.job.jobportal.Service.RedisService;
import com.job.jobportal.Service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/profile")
public class GetProfileController {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserService userservice;
    @Autowired
    private RedisService redisService;

    @GetMapping("/get")
    public ResponseEntity<?> getProfile() {
        try {
            String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
            UserInfo user = userservice.getUser(useremail);
            return ResponseEntity.ok(new UserResponseDto(
                    user.getId(),
                    user.getUsername(),
                    user.getUseremail(),
                    user.getUserrole().name()));
        } catch (Exception e) {
            log.error("Error fetching profile: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody UserDto userDto) {
        try {
            String useremail = SecurityContextHolder.getContext().getAuthentication().getName();
            UserInfo user = userservice.getUser(useremail);

            if (userDto.username() != null) {
                user.setUsername(userDto.username());
            }
            if (userDto.userpassword() != null) {
                user.setUserpassword(passwordEncoder.encode(userDto.userpassword()));
            }

            userservice.save(user);
            redisService.delete("users");

            return ResponseEntity.ok(new UserResponseDto(
                    user.getId(),
                    user.getUsername(),
                    user.getUseremail(),
                    user.getUserrole().name()));
        } catch (Exception e) {
            log.error("Error updating profile: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
