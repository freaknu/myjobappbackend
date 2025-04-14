package com.job.jobportal.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class OAuth2Controller {

    @GetMapping("/google/url")
    public ResponseEntity<String> getGoogleAuthUrl() {
        return ResponseEntity.ok("/oauth2/authorization/google");
    }
}