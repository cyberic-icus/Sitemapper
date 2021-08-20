package com.example.Sitemapper.service;

import com.example.Sitemapper.db.entity.Link;
import com.example.Sitemapper.db.repo.SitemapRepo;
import lombok.AllArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SitemapService {
    private static final Set<String> map = new HashSet<String>();
    private String baseURL;

    // Dependencies
    private final SitemapRepo sitemapRepo;
    private final WebDriver driver;

    public Set<Link> getMap(String baseURL){
        this.baseURL = baseURL;
        extractLinksRecursive(driver, baseURL);
        List<String> Links = new ArrayList<>(map);
        Collections.sort(Links);

        return Links
                .stream()
                .map(Link::new)
                .collect(Collectors.toSet());
    }

    private void extractLinksRecursive(WebDriver driver, String URL) {
        driver.get(URL);
        String html = driver.getPageSource();
        Document doc = Jsoup.parse(html);
        List<Element> links = doc.getElementsByAttribute("href");

        try {
            Set<String> uniqueLinks = links
                    .stream()
                    .map(link -> link.attr("href"))
                    .filter(link -> (link.startsWith("/")))
                    .map(link -> this.baseURL + link)
                    .collect(Collectors.toSet());

            for (String link : uniqueLinks) {
                try {
                    String url = new URI(link).toString();
                    if ((!(map.contains(url)))) {
                        map.add(url);
                        Thread.sleep(25000);
                        try {
                            extractLinksRecursive(driver, url);
                        } catch (Exception e) {

                        }
                    }
                } catch (Exception e) {
                    System.out.println("Invalid URL: " + link);
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            System.out.println("Some shitty urls out there");
            e.printStackTrace();
        }

    }

}
