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

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SitemapService {
    // Dependencies
    private final SitemapRepo sitemapRepo;
    private final WebDriver driver;



    private static final Set<String> map = new HashSet<String>();
    private static final Map<String, List<String>> robotsTxt = new HashMap<>();
    private static List<String> sitemapsLoc = new ArrayList<>();
    private static String site = "https://www.py4u.net";
    private static int crawlDelay = 15;
    private static int counter = 1;



    public Set<Link> getMap(String site) throws IOException {
        SitemapService.site = site;

        analyzeRobotsTxt(driver);
        if(sitemapsLoc.size()==0)
            generateMapRecursive(driver, site);
        else
            generateMapFromSitemap();


        List<String> Links = new ArrayList<>(map);
        Collections.sort(Links);
        return Links
                .stream()
                .map(Link::new)
                .collect(Collectors.toSet());
    }

    public static void analyzeRobotsTxt(WebDriver driver) throws IOException {
        driver.get(site + "/robots.txt");
        Document doc = Jsoup.parse(driver.getPageSource());
        try{
            Element text = doc.getElementsByTag("pre").get(0);
            String[] list = text.text().split("\n");
            for (String line : list) {
                String[] two = line.split(": ");
                if (two.length == 2) {
                    List<String> values = robotsTxt.get(two[0]);
                    if (values == null) values = new ArrayList<>();
                    values.add(two[1]);
                    robotsTxt.put(two[0].toLowerCase(Locale.ROOT), values);
                }
            }
            robotsTxt.forEach((key, value) -> System.out.println("KEY: " + key + " VALUE: " + value));

            List<String> crawlVals = robotsTxt.get("crawl-delay");
            if (crawlVals != null) {
                for (String s : crawlVals) {
                    if (Integer.parseInt(s) > crawlDelay) {
                        crawlDelay = Integer.parseInt(s);
                    }
                }
            }

            List<String> sitemaps = robotsTxt.get("sitemap");
            if (sitemaps != null) {
                sitemapsLoc = sitemaps;
            }
        }
        catch (Exception e){
            System.out.println();
        }
    }

    private static void generateMapFromSitemap(){

    }


    private static void generateMapRecursive(WebDriver driver, String baseURL) {
        driver.get(baseURL);
        String html = driver.getPageSource();
        Document doc = Jsoup.parse(html);
        List<Element> links = doc.getElementsByTag("a");

        links.forEach(System.out::println);

        WebDriverWait wait = new WebDriverWait(driver, 20);
        wait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));

        try {
            Set<String> uniqueLinks = links
                    .stream()
                    .map(link -> link.attr("href"))
                    .filter(link -> (link.startsWith("/")))
                    .map(link -> site + link)
                    .collect(Collectors.toSet());
            uniqueLinks.forEach(System.out::println);

            for (String link : uniqueLinks) {
                if (counter == 10) break;
                try {
                    String url = new URI(link).toString();
                    if ((!(map.contains(url)))) {
                        map.add(url);
                        System.out.println("URL №" + counter + " processing.");
                        counter++;
                        int timeout = ThreadLocalRandom.current().nextInt(crawlDelay, crawlDelay*2 + 1);
                        Thread.sleep(timeout);
                        try {
                            generateMapRecursive(driver, url);
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
