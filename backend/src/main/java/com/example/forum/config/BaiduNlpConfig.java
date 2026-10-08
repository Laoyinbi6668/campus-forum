
package com.example.forum.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "baidu.nlp")
@Data
public class BaiduNlpConfig {
    private String apiKey;
    private String secretKey;
    private String url;
    private String tokenUrl = "https://aip.baidubce.com/oauth/2.0/token";
}

