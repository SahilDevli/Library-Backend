package com.example.BookApp.service;

import com.example.BookApp.dto.*;
import com.example.BookApp.exception.ResourceNotFoundException;
import com.example.BookApp.model.Authors;
import com.example.BookApp.model.Books;
import com.example.BookApp.repository.AuthorRepository;
import com.example.BookApp.repository.BookRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BooksCRUD {

    private final BookRepository bRepo;
    private final AuthorRepository aRepo;

    public BooksCRUD(BookRepository bRepo, AuthorRepository aRepo) {
        this.bRepo = bRepo;
        this.aRepo = aRepo;
    }

    // ─────────────────────────────────────────
    //  PRIVATE HELPERS
    // ─────────────────────────────────────────

    private void validateRequest(BookRequestDto req) {
        Objects.requireNonNull(req,                            "book request must not be null");
        Objects.requireNonNull(req.getTitle(),                 "title must not be null");
        Objects.requireNonNull(req.getAuthor(),                "author must not be null");
        Objects.requireNonNull(req.getAuthor().getFirstName(), "author.firstName must not be null");
        Objects.requireNonNull(req.getAuthor().getLastName(),  "author.lastName must not be null");
        Objects.requireNonNull(req.getAuthor().getEmailId(),   "author.emailId must not be null");
    }

    private Authors findOrCreateAuthor(AuthorDto dto) {
        Integer existingId = aRepo.checkDuplicateInDB(dto.getEmailId());
        if (existingId != null) {
            return aRepo.findById(existingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + existingId));
        }
        Authors newAuthor = new Authors();
        newAuthor.setFirstName(dto.getFirstName());
        newAuthor.setLastName(dto.getLastName());
        newAuthor.setEmailId(dto.getEmailId());
        newAuthor.setBio(dto.getBio());
        return aRepo.save(newAuthor);
    }

    private boolean isBookDuplicate(String title, String authorFullName) {
        return bRepo.checkDuplicateInDB(title, authorFullName) != null;
    }

    private BookResponseDto toBookResponse(Books book) {
        String authorName = book.getAuthor().getFirstName() + " " + book.getAuthor().getLastName();
        return new BookResponseDto(book.getTitle(), authorName, book.getGenre(), book.getBook_content());
    }

    // ─────────────────────────────────────────
    //  CREATE
    // ─────────────────────────────────────────

    @Transactional
    public SaveBooksResponse saveBooks(List<BookRequestDto> requests) {
        Objects.requireNonNull(requests, "request list must not be null");

        int saved = 0, skipped = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            BookRequestDto req = requests.get(i);
            try {
                validateRequest(req);
                Authors author = findOrCreateAuthor(req.getAuthor());

                String authorFullName = author.getFirstName() + author.getLastName();
                if (isBookDuplicate(req.getTitle(), authorFullName)) {
                    skipped++;
                    continue;
                }

                Books book = new Books();
                book.setTitle(req.getTitle());
                book.setGenre(req.getGenre());
                book.setBook_content(req.getContent());
                book.setAuthor(author);

                bRepo.save(book);
                saved++;

            } catch (NullPointerException e) {
                errors.add("Entry [" + i + "] skipped — " + e.getMessage());
            }
        }
        return new SaveBooksResponse(saved, skipped, errors);
    }

    // ─────────────────────────────────────────
    //  READ
    // ─────────────────────────────────────────

    public List<Books> showBooks() {
        return bRepo.showAllBooks();
    }

    public List<BookResponseDto> getBooksByTitle(String title) {
        Objects.requireNonNull(title, "title must not be null");
        List<Books> books = bRepo.findByTitle(title);
        if (books.isEmpty()) throw new ResourceNotFoundException("No books found with title: " + title);
        return books.stream().map(this::toBookResponse).collect(Collectors.toList());
    }

    public List<BookResponseDto> getBooksByGenre(String genre) {
        Objects.requireNonNull(genre, "genre must not be null");
        List<Books> books = bRepo.findByGenre(genre);
        if (books.isEmpty()) throw new ResourceNotFoundException("No books found with genre: " + genre);
        return books.stream().map(this::toBookResponse).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────────────

    @Transactional
    public Books updateBook(int id, BookUpdateDto dto) {
        Books book = bRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        if (dto.getTitle() != null)   book.setTitle(dto.getTitle());
        if (dto.getGenre() != null)   book.setGenre(dto.getGenre());
        if (dto.getContent() != null) book.setBook_content(dto.getContent());

        if (dto.getAuthorId() != null) {
            Authors author = aRepo.findById(dto.getAuthorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + dto.getAuthorId()));
            book.setAuthor(author);
        }
        return bRepo.save(book);
    }

    // ─────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────

    @Transactional
    public String deleteBook(int id) {
        if (!bRepo.existsById(id)) {
            throw new ResourceNotFoundException("Book not found with id: " + id);
        }
        bRepo.deleteById(id);
        return "Book with id " + id + " deleted successfully";
    }
}