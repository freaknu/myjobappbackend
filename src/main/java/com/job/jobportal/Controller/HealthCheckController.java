package com.job.jobportal.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/actuator")
public class HealthCheckController {
    @GetMapping("/health")
    public String healthCheck() {
        return "OK";
    }
}
