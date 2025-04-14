package com.job.jobportal.Service;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PdfParseService {
    public String extractTextFromPdf(MultipartFile file) throws IOException {
        if (!file.getContentType().equals("application/pdf")) {
            throw new IllegalArgumentException("Only PDF files are supported");
        }

        try (InputStream inputStream = file.getInputStream();
                PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {

            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}
