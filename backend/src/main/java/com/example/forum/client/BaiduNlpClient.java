
package com.example.forum.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.forum.config.BaiduNlpConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class BaiduNlpClient {

    private final RestTemplate restTemplate;
    private final BaiduNlpConfig config;
    private String accessToken;
    private long tokenExpireTime;

    public BaiduNlpClient(BaiduNlpConfig config) {
        this.restTemplate = new RestTemplate();
        this.config = config;
        
        StringHttpMessageConverter converter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        restTemplate.getMessageConverters().removeIf(m -> m.getClass().equals(StringHttpMessageConverter.class));
        restTemplate.getMessageConverters().add(0, converter);
    }

    public SentimentResult analyzeSentiment(String text) {
        try {
            log.info("Starting sentiment analysis for text (length: {})", text.length());
            ensureTokenValid();
            
            String url = config.getUrl() + "?access_token=" + accessToken;
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("text", text);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
            
            String jsonString = JSON.toJSONString(requestBody);
            HttpEntity<String> entity = new HttpEntity<>(jsonString, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                String.class
            );
            
            log.info("Response status: {}", response.getStatusCode());
            
            String responseBody = response.getBody();
            log.info("Response body length: {}", responseBody != null ? responseBody.length() : 0);
            
            JSONObject jsonResponse = JSON.parseObject(responseBody);

            if (jsonResponse.containsKey("error_code")) {
                int errorCode = jsonResponse.getIntValue("error_code");
                String errorMsg = jsonResponse.getString("error_msg");
                log.error("Baidu NLP API error: code={}, msg={}", errorCode, errorMsg);
                return null;
            }

            JSONObject items = jsonResponse.getJSONArray("items").getJSONObject(0);
            SentimentResult result = new SentimentResult();
            result.setPositiveProb(items.getDouble("positive_prob"));
            result.setNegativeProb(items.getDouble("negative_prob"));
            result.setSentiment(items.getInteger("sentiment"));
            log.info("Analysis result: positive={}, negative={}, sentiment={}", 
                     result.getPositiveProb(), result.getNegativeProb(), result.getSentiment());
            return result;

        } catch (Exception e) {
            log.error("Failed to call Baidu NLP API", e);
            return null;
        }
    }

    private void ensureTokenValid() {
        if (accessToken == null || System.currentTimeMillis() >= tokenExpireTime) {
            refreshToken();
        }
    }

    private void refreshToken() {
        try {
            log.info("Refreshing Baidu NLP access token...");
            String url = config.getTokenUrl() + "?grant_type=client_credentials&client_id=" + config.getApiKey() + "&client_secret=" + config.getSecretKey();
            ResponseEntity response = restTemplate.getForEntity(url, String.class);
            log.info("Token refresh response: {}", response.getBody());
            
            JSONObject json = JSON.parseObject((String) response.getBody());
            if (json.containsKey("error")) {
                log.error("Error getting token: {}", json);
                return;
            }
            
            accessToken = json.getString("access_token");
            int expiresIn = json.getIntValue("expires_in");
            tokenExpireTime = System.currentTimeMillis() + (expiresIn - 300) * 1000L;
            log.info("Baidu NLP Access Token refreshed successfully");
        } catch (Exception e) {
            log.error("Failed to get Baidu NLP Access Token", e);
        }
    }

    @lombok.Data
    public static class SentimentResult {
        private Double positiveProb;
        private Double negativeProb;
        private Integer sentiment;
    }
}
