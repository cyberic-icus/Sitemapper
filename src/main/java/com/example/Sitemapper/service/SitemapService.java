package com.example.Sitemapper.service;

import com.example.Sitemapper.db.entity.Link;
import com.example.Sitemapper.db.repo.SitemapRepo;
import lombok.AllArgsConstructor;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

@Service
@AllArgsConstructor
public class SitemapService {
    private final SitemapRepo sitemapRepo;
    private final WebDriver driver;

    public Set<Link> getMap(){
        Set<Link> map = new HashSet<>();
        return map;
    }


}
