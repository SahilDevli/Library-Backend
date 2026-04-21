package com.example.BookApp.controller;

import com.example.BookApp.dto.BookRequestDto;
import com.example.BookApp.dto.BookResponseDto;
import com.example.BookApp.dto.BookUpdateDto;
import com.example.BookApp.dto.SaveBooksResponse;
import com.example.BookApp.model.Books;
import com.example.BookApp.service.BooksCRUD;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BooksController {

    private final BooksCRUD bookService;

    public BooksController(BooksCRUD bookService) {
        this.bookService = bookService;
    }

    // ── CREATE ───────────────────────────────────────────────────────────
    /** POST /api/books — bulk insert books with nested author (JWT required) */
    @PostMapping
    public ResponseEntity<@NotNull SaveBooksResponse> saveBooks(@RequestBody List<BookRequestDto> requests) {
        return ResponseEntity.ok(bookService.saveBooks(requests));
    }

    // ── READ ─────────────────────────────────────────────────────────────
    /** GET /api/books — all books */
    @GetMapping
    public ResponseEntity<@NotNull List<Books>> getAllBooks() {
        return ResponseEntity.ok(bookService.showBooks());
    }

    /** GET /api/books/title/{title} — returns title, authorName, genre, content */
    @GetMapping("/title/{title}")
    public ResponseEntity<@NotNull List<BookResponseDto>> getByTitle(@PathVariable String title) {
        return ResponseEntity.ok(bookService.getBooksByTitle(title));
    }

    /** GET /api/books/genre/{genre} — returns title, authorName, genre, content */
    @GetMapping("/genre/{genre}")
    public ResponseEntity<@NotNull List<BookResponseDto>> getByGenre(@PathVariable String genre) {
        return ResponseEntity.ok(bookService.getBooksByGenre(genre));
    }

    /** GET /api/books/getCSV — download all books as CSV file */
    @GetMapping("/getCSV")
    public void exportCSV(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"books.csv\"");

        List<Books> books = bookService.showBooks();
        PrintWriter writer = response.getWriter();

        // CSV header
        writer.println("book_id,title,genre,author_firstName,author_lastName,author_email,content");

        // CSV rows
        for (Books book : books) {
            writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    book.getBook_id(),
                    escapeCsv(book.getTitle()),
                    escapeCsv(book.getGenre()),
                    escapeCsv(book.getAuthor().getFirstName()),
                    escapeCsv(book.getAuthor().getLastName()),
                    escapeCsv(book.getAuthor().getEmailId()),
                    escapeCsv(book.getBook_content())
            );
        }
        writer.flush();
    }

    // ── UPDATE ───────────────────────────────────────────────────────────
    /** PATCH /api/books/{id} — update title/authorId/genre/content (JWT required) */
    @PatchMapping("/{id}")
    public ResponseEntity<@NotNull Books> updateBook(@PathVariable int id, @RequestBody BookUpdateDto dto) {
        return ResponseEntity.ok(bookService.updateBook(id, dto));
    }

    // ── DELETE ───────────────────────────────────────────────────────────
    /** DELETE /api/books/{id} — deletes book only, author untouched (JWT required) */
    @DeleteMapping("/{id}")
    public ResponseEntity<@NotNull String> deleteBook(@PathVariable int id) {
        return ResponseEntity.ok(bookService.deleteBook(id));
    }

    /** Escapes double quotes in CSV fields */
    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}
