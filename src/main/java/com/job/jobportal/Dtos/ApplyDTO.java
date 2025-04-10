package com.job.jobportal.Dtos;

import jakarta.validation.constraints.NotBlank;

public record ApplyDTO(
        @NotBlank String firstname,
        @NotBlank String lastname,
        String email) {
}               