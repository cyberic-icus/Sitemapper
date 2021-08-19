package com.example.Sitemapper.db.repo;

import com.example.Sitemapper.db.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LinkRepo extends JpaRepository<Link, UUID> {
}
