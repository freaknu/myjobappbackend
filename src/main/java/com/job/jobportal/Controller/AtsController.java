package com.job.jobportal.Controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.job.jobportal.Service.AtsScoringService;
import com.job.jobportal.Service.PdfParseService;
import com.job.jobportal.Service.AtsScoringService.AtsScoreResult;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class AtsController {
    @Autowired
    private PdfParseService pdfparse;
    @Autowired
    private AtsScoringService atsservice;

    @PostMapping("ats/score")
    public ResponseEntity<?> GetAtsScore(@RequestParam("resume") MultipartFile resume,
            @RequestParam("jobDescription") String jobDescription) {
        try {
            String resumetext = pdfparse.extractTextFromPdf(resume);
            AtsScoreResult atsresult = atsservice.calculateScore(resumetext, jobDescription);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", atsresult));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to process resume: " + e.getMessage()));
        }
    }

}
