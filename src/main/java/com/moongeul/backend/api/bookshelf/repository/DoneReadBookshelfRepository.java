package com.moongeul.backend.api.bookshelf.repository;

import com.moongeul.backend.api.bookshelf.entity.DoneReadBookshelf;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoneReadBookshelfRepository extends JpaRepository<DoneReadBookshelf, Long> {
}

