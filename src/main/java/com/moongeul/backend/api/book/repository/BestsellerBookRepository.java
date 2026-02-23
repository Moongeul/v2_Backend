package com.moongeul.backend.api.book.repository;

import com.moongeul.backend.api.book.entity.BestsellerBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BestsellerBookRepository extends JpaRepository<BestsellerBook, Long> {

    List<BestsellerBook> findAllByOrderBySortOrderAsc();
}
