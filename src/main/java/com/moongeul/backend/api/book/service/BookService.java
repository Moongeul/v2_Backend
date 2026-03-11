package com.moongeul.backend.api.book.service;

import com.moongeul.backend.api.book.dto.*;
import com.moongeul.backend.api.book.entity.BestsellerBook;
import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.book.repository.BestsellerBookRepository;
import com.moongeul.backend.api.book.repository.BookRepository;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.entity.Role;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.common.exception.BadRequestException;
import com.moongeul.backend.common.exception.ForbiddenException;
import com.moongeul.backend.common.exception.InternalServerException;
import com.moongeul.backend.common.exception.NotFoundException;
import com.moongeul.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final WebClient webClient;
    private final BookRepository bookRepository;
    private final BestsellerBookRepository bestsellerBookRepository;
    private final MemberRepository memberRepository;

    @Value("${naver.book.client-id}")
    private String clientId;

    @Value("${naver.book.client-secret}")
    private String clientSecret;

    // 도서/사용자 통합 검색
    @Transactional
    public BookSearchResponseDTO searchBooks(BookSearchRequestDTO bookSearchRequestDTO) {
        String searchType = resolveSearchType(bookSearchRequestDTO.getType());

        SearchPageResult<BookDTO> bookResult = SearchPageResult.empty();
        SearchPageResult<BookSearchUserDTO> userResult = SearchPageResult.empty();

        if ("book".equals(searchType) || "all".equals(searchType)) {
            bookResult = searchBookData(bookSearchRequestDTO);
        }

        if ("user".equals(searchType) || "all".equals(searchType)) {
            userResult = searchUserData(bookSearchRequestDTO);
        }

        int total;
        int totalPages;
        boolean isLast;

        if ("book".equals(searchType)) {
            total = bookResult.total;
            totalPages = bookResult.totalPages;
            isLast = bookResult.isLast;
        } else if ("user".equals(searchType)) {
            total = userResult.total;
            totalPages = userResult.totalPages;
            isLast = userResult.isLast;
        } else {
            total = bookResult.total + userResult.total;
            totalPages = Math.max(bookResult.totalPages, userResult.totalPages);
            isLast = bookResult.isLast && userResult.isLast;
        }

        BookSearchResponseDTO.SearchResultData searchData = BookSearchResponseDTO.SearchResultData.builder()
                .bookData(bookResult.data)
                .userData(userResult.data)
                .build();

        return BookSearchResponseDTO.builder()
                .type(searchType)
                .total(total)
                .page(bookSearchRequestDTO.getPage())
                .size(bookSearchRequestDTO.getSize())
                .totalPages(totalPages)
                .isLast(isLast)
                .data(searchData)
                .build();
    }

    private SearchPageResult<BookDTO> searchBookData(BookSearchRequestDTO bookSearchRequestDTO) {
        NaverBookSearchResponseDTO naverBookSearchResponseDTO = callNaverBookAPI(bookSearchRequestDTO);

        if (naverBookSearchResponseDTO.getItems() == null || naverBookSearchResponseDTO.getItems().isEmpty()) {
            return SearchPageResult.empty();
        }

        // ISBN 리스트 추출 (첫 번째 ISBN만 사용)
        List<String> isbns = naverBookSearchResponseDTO.getItems().stream()
                .map(item -> {
                    if (item.getIsbn() == null || item.getIsbn().isEmpty()) {
                        return null;
                    }
                    return item.getIsbn().split(" ")[0].trim();
                })
                .filter(isbn -> isbn != null && !isbn.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        // DB에서 기존 책 조회
        List<Book> existingBooks = bookRepository.findByIsbnIn(isbns);
        Map<String, Book> existingBookMap = existingBooks.stream()
                .collect(Collectors.toMap(Book::getIsbn, book -> book));

        // 책 정보 저장/업데이트
        List<BookDTO> bookDTOs = new ArrayList<>();
        for (NaverBookItemDTO naverItem : naverBookSearchResponseDTO.getItems()) {
            if (naverItem.getIsbn() == null || naverItem.getIsbn().isEmpty()) {
                continue;
            }

            String isbn = naverItem.getIsbn().split(" ")[0].trim();
            Book book = existingBookMap.get(isbn);

            if (book != null) {
                updateBookIfChanged(book, naverItem);
            } else {
                book = saveNewBook(naverItem, isbn);
            }

            bookDTOs.add(convertToDTO(book));
        }

        int total = naverBookSearchResponseDTO.getTotal() != null ? naverBookSearchResponseDTO.getTotal() : 0;
        int totalPages = (int) Math.ceil((double) total / bookSearchRequestDTO.getSize());
        int start = (bookSearchRequestDTO.getPage() - 1) * bookSearchRequestDTO.getSize() + 1;
        boolean isLast = (start + bookSearchRequestDTO.getSize() - 1) >= total;

        return SearchPageResult.of(total, totalPages, isLast, bookDTOs);
    }

    private SearchPageResult<BookSearchUserDTO> searchUserData(BookSearchRequestDTO bookSearchRequestDTO) {
        Pageable pageable = PageRequest.of(bookSearchRequestDTO.getPage() - 1, bookSearchRequestDTO.getSize());
        Page<Member> memberPage = memberRepository.findByNicknameContainingIgnoreCase(bookSearchRequestDTO.getQuery(), pageable);

        List<BookSearchUserDTO> userDTOList = memberPage.getContent().stream()
                .map(member -> BookSearchUserDTO.builder()
                        .userId(member.getId())
                        .profileImage(member.getProfileImage())
                        .nickname(member.getNickname())
                        .readingTasteType(member.getReadingTasteType())
                        .build())
                .toList();

        return SearchPageResult.of(
                (int) memberPage.getTotalElements(),
                memberPage.getTotalPages(),
                memberPage.isLast(),
                userDTOList
        );
    }

    private String resolveSearchType(String rawType) {
        String type = StringUtils.hasText(rawType) ? rawType.trim().toLowerCase(Locale.ROOT) : "all";

        if (!type.equals("book") && !type.equals("user") && !type.equals("all")) {
            throw new BadRequestException(ErrorStatus.INVALID_SEARCH_TYPE_EXCEPTION.getMessage());
        }

        return type;
    }

    private NaverBookSearchResponseDTO callNaverBookAPI(BookSearchRequestDTO request) {
        // 네이버 API 파라미터 설정
        int start = (request.getPage() - 1) * request.getSize() + 1;
        int display = Math.min(request.getSize(), 100); // 네이버 API 최대 100개

        // URI 생성 (보여준 코드 방식 적용)
        URI uri = UriComponentsBuilder
                .fromUriString("https://openapi.naver.com")
                .path("/v1/search/book.json")
                .queryParam("query", request.getQuery())
                .queryParam("display", display)
                .queryParam("start", start)
                .encode()
                .build()
                .toUri();

        log.info("네이버 도서 API 호출 URL: {}", uri);
        log.info("검색어: {}", request.getQuery());

        try {
            // 먼저 응답 본문을 String으로 받아서 확인
            String responseBody = webClient.get()
                    .uri(uri)
                    .header("X-Naver-Client-Id", clientId)
                    .header("X-Naver-Client-Secret", clientSecret)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                        log.error("네이버 API 4xx 에러 발생: {}", clientResponse.statusCode());
                        throw new InternalServerException(ErrorStatus.NAVER_SERVER_ERROR.getMessage());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, serverResponse -> {
                        log.error("네이버 API 5xx 에러 발생: {}", serverResponse.statusCode());
                        throw new InternalServerException(ErrorStatus.NAVER_SERVER_ERROR.getMessage());
                    })
                    .bodyToMono(String.class)
                    .block();

            log.info("네이버 API 원본 응답 (처음 500자): {}", 
                    responseBody != null && responseBody.length() > 500 
                            ? responseBody.substring(0, 500) 
                            : responseBody);

            // JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            NaverBookSearchResponseDTO response = objectMapper.readValue(responseBody, NaverBookSearchResponseDTO.class);

            if (response != null) {
                log.info("네이버 API 응답 파싱 성공 - total: {}, start: {}, display: {}, items 수: {}", 
                        response.getTotal(), 
                        response.getStart(),
                        response.getDisplay(),
                        response.getItems() != null ? response.getItems().size() : 0);
            } else {
                log.warn("네이버 API 응답이 null입니다.");
            }
            return response;
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("JSON 파싱 실패: {}", e.getMessage(), e);
            throw new InternalServerException(ErrorStatus.NAVER_SERVER_ERROR.getMessage());
        } catch (Exception e) {
            log.error("네이버 도서 API 호출 실패: {}", e.getMessage(), e);
            e.printStackTrace();
            throw new InternalServerException(ErrorStatus.NAVER_SERVER_ERROR.getMessage());
        }
    }

    private void updateBookIfChanged(Book book, NaverBookItemDTO naverItem) {
        String cleanIsbn = naverItem.getIsbn().split(" ")[0].trim();
        String cleanTitle = truncateString(cleanHtmlTags(naverItem.getTitle()), 255);
        String cleanAuthor = truncateString(cleanHtmlTags(naverItem.getAuthor()), 255);
        String cleanPublisher = truncateString(cleanHtmlTags(naverItem.getPublisher()), 255);
        String cleanDescription = cleanHtmlTags(naverItem.getDescription());

        // 데이터가 변경되었는지 확인
        boolean changed = !cleanTitle.equals(book.getTitle()) ||
                !cleanAuthor.equals(book.getAuthor()) ||
                !cleanPublisher.equals(book.getPublisher()) ||
                !cleanDescription.equals(book.getDescription()) ||
                !naverItem.getImage().equals(book.getBookImage()) ||
                !naverItem.getPubdate().equals(book.getPubdate());

        if (changed) {
            book.update(cleanTitle, cleanAuthor, naverItem.getImage(), 
                       cleanPublisher, cleanDescription, naverItem.getPubdate());
            bookRepository.save(book);
        }
    }

    private Book saveNewBook(NaverBookItemDTO naverItem, String isbn) {
        String cleanTitle = truncateString(cleanHtmlTags(naverItem.getTitle()), 255);
        String cleanAuthor = truncateString(cleanHtmlTags(naverItem.getAuthor()), 255);
        String cleanPublisher = truncateString(cleanHtmlTags(naverItem.getPublisher()), 255);
        String cleanDescription = cleanHtmlTags(naverItem.getDescription());
        String bookImage = truncateString(naverItem.getImage(), 255);

        Book newBook = Book.builder()
                .isbn(isbn)
                .title(cleanTitle)
                .author(cleanAuthor)
                .bookImage(bookImage)
                .publisher(cleanPublisher)
                .description(cleanDescription)
                .pubdate(naverItem.getPubdate())
                .ratingAverage(0.0)
                .ratingCount(0)
                .build();

        return bookRepository.save(newBook);
    }

    private BookDTO convertToDTO(Book book) {
        return BookDTO.builder()
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .bookImage(book.getBookImage())
                .publisher(book.getPublisher())
                .description(book.getDescription())
                .pubdate(book.getPubdate())
                .ratingAverage(book.getRatingAverage())
                .ratingCount(book.getRatingCount())
                .build();
    }

    // 네이버 API 응답에서 HTML 태그 제거
    private String cleanHtmlTags(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("<[^>]*>", "").trim();
    }

    // 문자열을 지정된 길이로 자르기
    private String truncateString(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

    // 책 상세 정보 조회
    @Transactional(readOnly = true)
    public BookDTO getBookDetail(String isbn) {
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.BOOK_NOTFOUND_EXCEPTION.getMessage()));
        
        return convertToDTO(book);
    }

    // 베스트셀러 ISBN 등록 (관리자 전용)
    @Transactional
    public void registerBestsellerBooks(String email, BestsellerRegisterRequestDTO requestDTO) {
        Member member = getMemberByEmail(email);
        validateAdmin(member);

        List<String> normalizedIsbnList = normalizeIsbnList(requestDTO.getIsbnList());
        validateBestsellerIsbnList(normalizedIsbnList);

        List<Book> books = bookRepository.findByIsbnIn(normalizedIsbnList);
        Map<String, Book> bookMap = books.stream()
                .collect(Collectors.toMap(Book::getIsbn, book -> book));

        if (bookMap.size() != normalizedIsbnList.size()) {
            throw new NotFoundException(ErrorStatus.BESTSELLER_BOOK_NOT_FOUND_EXCEPTION.getMessage());
        }

        bestsellerBookRepository.deleteAllInBatch();

        List<BestsellerBook> bestsellerBooks = new ArrayList<>();
        for (int i = 0; i < normalizedIsbnList.size(); i++) {
            String isbn = normalizedIsbnList.get(i);
            bestsellerBooks.add(BestsellerBook.builder()
                    .sortOrder(i + 1)
                    .book(bookMap.get(isbn))
                    .build());
        }

        bestsellerBookRepository.saveAll(bestsellerBooks);
    }

    // 베스트셀러 조회
    @Transactional(readOnly = true)
    public BestsellerBookListResponseDTO getBestsellerBooks() {
        List<BestsellerBookItemDTO> bookList = bestsellerBookRepository.findAllByOrderBySortOrderAsc().stream()
                .map(bestsellerBook -> BestsellerBookItemDTO.builder()
                        .isbn(bestsellerBook.getBook().getIsbn())
                        .bookImage(bestsellerBook.getBook().getBookImage())
                        .title(bestsellerBook.getBook().getTitle())
                        .author(bestsellerBook.getBook().getAuthor())
                        .build())
                .toList();

        return BestsellerBookListResponseDTO.builder()
                .data(bookList)
                .build();
    }

    private List<String> normalizeIsbnList(List<String> isbnList) {
        List<String> normalizedList = new ArrayList<>();
        for (String isbn : isbnList) {
            if (!StringUtils.hasText(isbn)) {
                throw new BadRequestException(ErrorStatus.BESTSELLER_INVALID_ISBN_EXCEPTION.getMessage());
            }
            normalizedList.add(isbn.trim());
        }
        return normalizedList;
    }

    private void validateBestsellerIsbnList(List<String> isbnList) {
        if (isbnList.isEmpty() || isbnList.size() > 10) {
            throw new BadRequestException(ErrorStatus.BESTSELLER_LIMIT_EXCEEDED_EXCEPTION.getMessage());
        }

        Set<String> isbnSet = new LinkedHashSet<>(isbnList);
        if (isbnSet.size() != isbnList.size()) {
            throw new BadRequestException(ErrorStatus.BESTSELLER_DUPLICATE_ISBN_EXCEPTION.getMessage());
        }
    }

    private void validateAdmin(Member member) {
        if (member.getRole() != Role.ADMIN) {
            throw new ForbiddenException(ErrorStatus.ADMIN_FORBIDDEN_EXCEPTION.getMessage());
        }
    }

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));
    }

    private static class SearchPageResult<T> {
        private final int total;
        private final int totalPages;
        private final boolean isLast;
        private final List<T> data;

        private SearchPageResult(int total, int totalPages, boolean isLast, List<T> data) {
            this.total = total;
            this.totalPages = totalPages;
            this.isLast = isLast;
            this.data = data;
        }

        private static <T> SearchPageResult<T> of(int total, int totalPages, boolean isLast, List<T> data) {
            return new SearchPageResult<>(total, totalPages, isLast, data);
        }

        private static <T> SearchPageResult<T> empty() {
            return new SearchPageResult<>(0, 0, true, new ArrayList<>());
        }
    }
}
