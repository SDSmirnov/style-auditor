package com.styleauditor.api;

import com.styleauditor.engine.AnalysisService;
import com.styleauditor.model.AnalysisResult;
import com.styleauditor.model.AnalyzeRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AnalyzeController {
    private final AnalysisService service;

    public AnalyzeController(AnalysisService service) {
        this.service = service;
    }

    private static final int MAX_CHARS = 200_000;

    @PostMapping("/analyze")
    public ResponseEntity<?> analyze(@RequestBody(required = false) AnalyzeRequest request) {
        if (request == null || request.text() == null || request.text().isBlank()) {
            return ResponseEntity.badRequest().body("text is required");
        }
        if (request.text().length() > MAX_CHARS) {
            return ResponseEntity.badRequest().body("text exceeds maximum length of " + MAX_CHARS + " characters");
        }
        return ResponseEntity.ok(service.analyze(request.text()));
    }
}

