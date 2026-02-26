package com.moongeul.backend.api.book.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "BESTSELLER_BOOK")
public class BestsellerBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer sortOrder; // 노출 순서

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_isbn", nullable = false, unique = true)
    private Book book;
}
