package com.example.Sitemapper.db.dto.mapper;

import com.example.Sitemapper.db.dto.response.ResponseForSitemapDTO;
import com.example.Sitemapper.db.entity.Sitemap;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract  class SitemapMapper {
        public abstract ResponseForSitemapDTO SitemapToResponse(Sitemap sitemap);
}
