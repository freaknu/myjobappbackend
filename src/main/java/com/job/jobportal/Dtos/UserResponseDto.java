package com.job.jobportal.Dtos;

public record UserResponseDto(
        String id,
        String username,
        String useremail,
        String role) {
}