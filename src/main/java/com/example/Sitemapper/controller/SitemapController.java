package com.example.Sitemapper.controller;

import com.example.Sitemapper.db.dto.mapper.SitemapMapper;
import com.example.Sitemapper.db.dto.response.RequestForSitemapDTO;
import com.example.Sitemapper.db.dto.response.ResponseForSitemapDTO;
import com.example.Sitemapper.db.entity.Link;
import com.example.Sitemapper.db.entity.Sitemap;
import com.example.Sitemapper.db.repo.LinkRepo;
import com.example.Sitemapper.db.repo.SitemapRepo;
import com.example.Sitemapper.service.SitemapService;
import lombok.AllArgsConstructor;
import org.jsoup.select.Collector;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/")
@AllArgsConstructor
public class SitemapController {
    private final SitemapService service;
    private final SitemapRepo repo;
    private final SitemapMapper mapper;
    private final LinkRepo linkRepo;

    @PostMapping("request/")
    ResponseForSitemapDTO requestForSitemap(@RequestBody RequestForSitemapDTO request){
        try{
            String url = request.getBaseURL();
            URI uri = new URI(url);
            Set<Link> links = service.getMap(url)
                    .stream()
                    .map(link -> new Link(link))
                    .collect(Collectors.toSet());

            List<Link> linkList = linkRepo.saveAll(links);

            Sitemap sitemap = new Sitemap();
            sitemap.setBaseURL(url);
            sitemap.setMap(linkList);

            repo.save(sitemap);

            return mapper.SitemapToResponse(sitemap);


        } catch (URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


}
