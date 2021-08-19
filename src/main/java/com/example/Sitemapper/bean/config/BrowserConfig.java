package com.example.Sitemapper.bean.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "browser")
@Setter
@Getter
public class BrowserConfig {
    private String path;
}
