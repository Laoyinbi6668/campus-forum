
package com.example.forum.service;

import com.example.forum.client.BaiduNlpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SentimentAnalysisService {

    private final BaiduNlpClient baiduNlpClient;
    
    @Value("${system.warning.threshold:0.7}")
    private double warningThreshold;

    public SentimentAnalysisService(BaiduNlpClient baiduNlpClient) {
        this.baiduNlpClient = baiduNlpClient;
    }

    public SentimentResult analyze(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        try {
            BaiduNlpClient.SentimentResult result = baiduNlpClient.analyzeSentiment(text);
            if (result != null) {
                SentimentResult sentimentResult = new SentimentResult();
                sentimentResult.setPositiveProb(result.getPositiveProb());
                sentimentResult.setNegativeProb(result.getNegativeProb());
                sentimentResult.setSentiment(result.getSentiment());
                sentimentResult.setWarning(result.getNegativeProb() >= warningThreshold);
                return sentimentResult;
            }
        } catch (Exception e) {
            log.error("Sentiment analysis failed", e);
        }
        return null;
    }

    @lombok.Data
    public static class SentimentResult {
        private Double positiveProb;
        private Double negativeProb;
        private Integer sentiment;
        private Boolean warning;
    }
}

