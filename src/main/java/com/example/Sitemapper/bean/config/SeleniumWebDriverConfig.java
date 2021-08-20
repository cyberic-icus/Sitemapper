package com.example.Sitemapper.bean.config;

import lombok.AllArgsConstructor;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.List;

@Configuration
@AllArgsConstructor
public class SeleniumWebDriverConfig {
    private final Environment env;
    private WebDriver driver;

    @Bean(destroyMethod = "close")
    public WebDriver driver(){
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--headless",
                "--disable-gpu",
                "--window-size=1920,1200",
                "--ignore-certificate-errors"
        );
        options.setBinary(env.getProperty("browser.path"));
        driver = new ChromeDriver(options);
        return driver;
    }
    public void close(){
        driver.close();
    }

}
