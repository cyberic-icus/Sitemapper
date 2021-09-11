package com.example.Sitemapper.db.dto.response;

import com.example.Sitemapper.db.entity.Link;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseForSitemapDTO {
    private UUID id;
    private List<Link> map;
}
