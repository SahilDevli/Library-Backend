package com.example.BookApp.controller;

import com.example.BookApp.dto.AuthorBooksResponseDto;
import com.example.BookApp.dto.AuthorUpdateDto;
import com.example.BookApp.model.Authors;
import com.example.BookApp.service.AuthorsCRUD;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorsController {

    private final AuthorsCRUD authorService;

    public AuthorsController(AuthorsCRUD authorService) {
        this.authorService = authorService;
    }

    // ── READ ─────────────────────────────────────────────────────────────
    /** GET /api/authors — all authors */
    @GetMapping
    public ResponseEntity<@NotNull List<Authors>> getAllAuthors() {
        return ResponseEntity.ok(authorService.getAuthors());
    }

    /** GET /api/authors/search?name=George — returns authorName, bio, list of books (title, genre) */
    @GetMapping("/search")
    public ResponseEntity<@NotNull List<AuthorBooksResponseDto>> searchByName(@RequestParam String name) {
        return ResponseEntity.ok(authorService.getAuthorsByName(name));
    }

    // ── UPDATE ───────────────────────────────────────────────────────────
    /** PATCH /api/authors/{id} — update firstName/lastName/email/bio (auth required) */
    @PatchMapping("/{id}")
    public ResponseEntity<@NotNull Authors> updateAuthor(@PathVariable int id, @RequestBody AuthorUpdateDto dto) {
        return ResponseEntity.ok(authorService.updateAuthor(id, dto));
    }

    // ── DELETE ───────────────────────────────────────────────────────────
    /** DELETE /api/authors/{id} — fails if author still has books (auth required) */
    @DeleteMapping("/{id}")
    public ResponseEntity<@NotNull String> deleteAuthor(@PathVariable int id) {
        return ResponseEntity.ok(authorService.deleteAuthor(id));
    }
}

