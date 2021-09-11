package com.example.Sitemapper.db.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

@Entity
@Setter
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Sitemap {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "VARCHAR(255)")
    private UUID id;
    private String baseURL;
    private boolean isReady = false;

    @OneToMany(mappedBy = "sitemap", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Link> map = new ArrayList<>();


}
