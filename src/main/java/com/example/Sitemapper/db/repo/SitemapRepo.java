package com.example.Sitemapper.db.repo;

import com.example.Sitemapper.db.entity.Sitemap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SitemapRepo extends JpaRepository<Sitemap, UUID> {
}
