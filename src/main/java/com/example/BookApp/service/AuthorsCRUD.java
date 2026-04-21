package com.example.BookApp.service;

import com.example.BookApp.dto.AuthorBooksResponseDto;
import com.example.BookApp.dto.AuthorUpdateDto;
import com.example.BookApp.dto.BookSummaryDto;
import com.example.BookApp.exception.ResourceNotFoundException;
import com.example.BookApp.model.Authors;
import com.example.BookApp.repository.AuthorRepository;
import com.example.BookApp.repository.BookRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AuthorsCRUD {

    private final AuthorRepository aRepo;
    private final BookRepository bRepo;

    public AuthorsCRUD(AuthorRepository aRepo, BookRepository bRepo) {
        this.aRepo = aRepo;
        this.bRepo = bRepo;
    }

    // ─────────────────────────────────────────
    //  PRIVATE HELPERS
    // ─────────────────────────────────────────

    private AuthorBooksResponseDto toAuthorResponse(Authors author) {
        List<BookSummaryDto> bookSummaries = author.getBooks() == null
                ? List.of()
                : author.getBooks().stream()
                        .map(b -> new BookSummaryDto(b.getTitle(), b.getGenre()))
                        .collect(Collectors.toList());
        return new AuthorBooksResponseDto(author.getFirstName(), author.getLastName(), author.getBio(), bookSummaries);
    }

    // ─────────────────────────────────────────
    //  CREATE
    // ─────────────────────────────────────────

    @Transactional
    public Boolean saveAuthor(Authors author) {
        Objects.requireNonNull(author, "author must not be null");
        Objects.requireNonNull(author.getEmailId(), "emailId must not be null");

        if (aRepo.checkDuplicateInDB(author.getEmailId()) == null) {
            aRepo.save(author);
            return true;
        }
        return false; // already exists
    }

    // ─────────────────────────────────────────
    //  READ
    // ─────────────────────────────────────────

    public List<Authors> getAuthors() {
        return aRepo.findAll();
    }

    @Transactional
    public List<AuthorBooksResponseDto> getAuthorsByName(String name) {
        Objects.requireNonNull(name, "name must not be null");
        List<Authors> authors = aRepo.findByNameContaining(name);
        if (authors.isEmpty()) throw new ResourceNotFoundException("No authors found with name: " + name);
        return authors.stream().map(this::toAuthorResponse).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────────────

    @Transactional
    public Authors updateAuthor(int id, AuthorUpdateDto dto) {
        Authors author = aRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));

        if (dto.getFirstName() != null) author.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null)  author.setLastName(dto.getLastName());
        if (dto.getBio() != null)       author.setBio(dto.getBio());
        if (dto.getEmailId() != null)   author.setEmailId(dto.getEmailId());

        return aRepo.save(author);
    }

    // ─────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────

    @Transactional
    public String deleteAuthor(int id) {
        if (!aRepo.existsById(id)) {
            throw new ResourceNotFoundException("Author not found with id: " + id);
        }
        long bookCount = bRepo.countByAuthorId(id);
        if (bookCount > 0) {
            throw new IllegalArgumentException(
                "Cannot delete author with id " + id + " — they have " + bookCount + " book(s). Delete the books first."
            );
        }
        aRepo.deleteById(id);
        return "Author with id " + id + " deleted successfully";
    }
}
