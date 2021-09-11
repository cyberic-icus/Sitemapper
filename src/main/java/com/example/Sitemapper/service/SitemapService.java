package com.example.Sitemapper.service;

import com.example.Sitemapper.db.repo.SitemapRepo;
import lombok.AllArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SitemapService {
    private static final Set<String> MAP = new HashSet<>();
    private static final Map<String, List<String>> ROBOTS_TXT = new HashMap<>();
    private static String SITE;
    private static List<String> SITEMAP_FROM_ROBOTS = new ArrayList<>();
    private static int CRAWL_DELAY = 15;


    // Dependencies
    private final SitemapRepo sitemapRepo;
    private final WebDriver driver;

    /*
        Этот метод анализирует файл robots.txt - файл спецификации для индексации ботами поисковых систем.
        В частности он ищет поля crawl-delay (указывает минимальный интервал между запросами),
        и sitemap (указывает на урл, где находиться файл с картой сайта),
        и если их находит, то значение crawl-delay устанавливается в CRAWL_DELAY,
        а значение sitemap помещает в SITEMAP_FROM_ROBOTS
     */
    private static void analyzeRobotsTxt(WebDriver driver) throws InterruptedException {
        String page = getPageWithTimeout(driver, SITE + "robots.txt");
        Document doc = Jsoup.parse(page);
        try {
            // Если открыть robots.txt, то вся информация содержиться как plain text в блоке pre.
            Element text = doc.getElementsByTag("pre").get(0);

            // Этот блок кода превращает текст в вид Map<String, List<String>>
            String[] list = text.text().split("\n");
            for (String line : list) {
                String[] two = line.split(": ");
                if (two.length == 2) {
                    List<String> values = ROBOTS_TXT.get(two[0]);
                    if (values == null) values = new ArrayList<>();
                    values.add(two[1]);
                    ROBOTS_TXT.put(two[0].toLowerCase(Locale.ROOT), values);
                }
            }
            ROBOTS_TXT.forEach((key, value) -> System.out.println("KEY: " + key + " VALUE: " + value));

            // Если поле crawl-delay указано - устанавливаем значение
            List<String> crawlVals = ROBOTS_TXT.get("crawl-delay");
            if (crawlVals != null) {
                for (String s : crawlVals) {
                    if (Integer.parseInt(s) > CRAWL_DELAY) {
                        CRAWL_DELAY = Integer.parseInt(s);
                    }
                }
            }
            // Если поле sitemap указано - устанавливаем значение
            List<String> sitemaps = ROBOTS_TXT.get("sitemap");
            if (sitemaps != null) {
                SITEMAP_FROM_ROBOTS = sitemaps;
            }
        } catch (Exception e) {
            System.out.println();
        }
    }

    /*
        Этот метод анализирует содержимое SITEMAP_FROM_ROBOTS и рекурсивно ищет урлы.
        В SITEMAP_FROM_ROBOTS могут находиться сайтмапы, которые указывают на другие сайтмапы, и которые указывают на урлы,
        поэтому тут рекурсивно обходятся содержимое и выискиваются сайтмапы и урлы.
     */
    private static void generateMapFromSitemap(WebDriver driver, List<String> sitemaps) throws InterruptedException {
        List<String> hiddenSitemaps = new ArrayList<>(); // тут будут храниться сайтмапы, на которые указывали сайтмапы из SITEMAP_FROM_ROBOTS
        for (String sitemap : sitemaps) {
            String xmlText = getPageWithTimeout(driver, sitemap);
            Document doc = Jsoup.parse(xmlText);

            // Находим все элементы, в которых есть SITE, проходимся по ним и определяем что это -
            // сайтмап или урл
            Elements elements = doc.getElementsContainingOwnText(SITE);
            for (Element e : elements) {
                String text = e.text();
                int textLength = text.length();
                int indexOfXML = text.indexOf(".xml");

                // если .xml находиться в конце urlа, то это сайтмап
                if (textLength - 4 == indexOfXML) {
                    System.out.println("SITEMAP " + text);
                    hiddenSitemaps.add(text);
                }
                // если .xml нет, то это урл
                else if (indexOfXML == -1) {
                    System.out.println("URL " + text);
                    MAP.add(text);
                }
            }

        }
        // Если хранилище сайтмап, на которые указывали сайтмапы, не пусто, то проходимся и по ним.
        if (hiddenSitemaps.size() != 0)
            generateMapFromSitemap(driver, hiddenSitemaps);

    }

    /*
        Этот метод рекурсивно генерирует карту сайта.
        Он ищет все ссылки на мейн странице и рекурсивно ищет по ссылкам другие ссылки.
     */
    private static void generateMapRecursive(WebDriver driver, String baseURL) {
        /*
            Некоторые сайты используют AJAX запросы для подгрузки данных,
            и чтобы нам достать все данные мы должны проскроллить до конца страницы,
            для этого мы используем js.
         */
        JavascriptExecutor js = (JavascriptExecutor) driver;
        driver.get(baseURL);
//        WebDriverWait wait = new WebDriverWait(driver, CRAWL_DELAY);
//        wait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");

        String html = driver.getPageSource();
        Document doc = Jsoup.parse(html);
        List<Element> links = doc.getElementsByTag("a");

        links.forEach(System.out::println);


        try {
            // Выбираем только уникальные ссылки, нам же не нужно обходить одну и ту же страницу несколько раз, так?
            Set<String> uniqueLinks = links
                    .stream()
                    .map(link -> link.attr("href"))
                    .filter(link -> (link.startsWith("/")))
                    .map(link -> SITE + link)
                    .collect(Collectors.toSet());
            uniqueLinks.forEach(System.out::println);

            for (String link : uniqueLinks) {
                try {
                    String url = new URI(link).toString();
                    if ((!(MAP.contains(url)))) {
                        MAP.add(url);
                        int timeout = ThreadLocalRandom.current().nextInt(CRAWL_DELAY, CRAWL_DELAY * 2 + 1) * 1000;
                        Thread.sleep(timeout);
                        try {
                            generateMapRecursive(driver, url);
                        } catch (Exception e) {
                            e.printStackTrace();
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
    /*
        Этот метод делает гет запрос через вебдрайвер с таймаутом и возвращает полученную страницу
     */

    private static String getPageWithTimeout(WebDriver driver, String path) throws InterruptedException {
        driver.get(path);
        int timeout = ThreadLocalRandom.current().nextInt(CRAWL_DELAY, CRAWL_DELAY * 2 + 1) * 1000;
        Thread.sleep(timeout);
        return driver.getPageSource();
    }

    /*
        Этот метод возвращает нам список ссылок.
        Мне кажется для большого количества ссылок он сожрет всю память
        и уничтожит компьютер.
     */
    public Set<String> getMap(String site) throws InterruptedException {
        SitemapService.SITE = site;

        analyzeRobotsTxt(driver);
        if (SITEMAP_FROM_ROBOTS.size() == 0)
            generateMapRecursive(driver, site);
        else
            generateMapFromSitemap(driver, SITEMAP_FROM_ROBOTS);


        return MAP;
    }


}
