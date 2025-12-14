package com.moongeul.backend.api.book.repository;

import com.moongeul.backend.api.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, String> {
    Optional<Book> findByIsbn(String isbn);
    
    List<Book> findByIsbnIn(List<String> isbns);
}

