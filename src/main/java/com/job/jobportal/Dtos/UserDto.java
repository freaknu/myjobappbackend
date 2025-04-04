package com.job.jobportal.Dtos;

import com.job.jobportal.Model.UserInfo.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserDto(
                @NotBlank String username,
                @Email String useremail,
                @Size(min = 8) String userpassword,
                @NotNull Role role) {
}