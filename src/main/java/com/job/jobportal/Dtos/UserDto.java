package com.job.jobportal.Dtos;

import com.job.jobportal.Model.UserInfo;

public record UserDto(
        String username,
        String useremail,
        String userpassword,
        UserInfo.Role role) {
}