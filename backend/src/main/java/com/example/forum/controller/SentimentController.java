
package com.example.forum.controller;

import com.example.forum.dto.response.ApiResponse;
import com.example.forum.service.SentimentAnalysisService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sentiment")
public class SentimentController {

    private final SentimentAnalysisService sentimentAnalysisService;

    public SentimentController(SentimentAnalysisService sentimentAnalysisService) {
        this.sentimentAnalysisService = sentimentAnalysisService;
    }

    @PostMapping("/analyze")
    public ApiResponse<SentimentAnalysisService.SentimentResult> analyze(@RequestBody AnalyzeRequest request) {
        SentimentAnalysisService.SentimentResult result = sentimentAnalysisService.analyze(request.getText());
        return ApiResponse.success(result);
    }

    @lombok.Data
    public static class AnalyzeRequest {
        private String text;
    }
}

