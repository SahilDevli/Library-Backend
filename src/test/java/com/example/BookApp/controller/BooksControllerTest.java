package com.example.BookApp.controller;

import com.example.BookApp.dto.*;
import com.example.BookApp.model.Authors;
import com.example.BookApp.model.Books;
import com.example.BookApp.service.BooksCRUD;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.BookApp.config.SecurityConfig;

@WebMvcTest(BooksController.class)
@Import(SecurityConfig.class)
@DisplayName("BooksController API Tests")
class BooksControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean BooksCRUD booksCRUD;

    private Books book;
    private BookResponseDto bookResponse;

    @BeforeEach
    void setUp() {
        Authors author = new Authors();
        author.setAuthor_id(1);
        author.setFirstName("George");
        author.setLastName("Orwell");
        author.setEmailId("george@books.com");

        book = new Books();
        book.setBook_id(1);
        book.setTitle("1984");
        book.setGenre("Fictional");
        book.setBook_content("A dystopian novel...");
        book.setAuthor(author);

        bookResponse = new BookResponseDto("1984", "George Orwell", "Fictional", "A dystopian novel...");
    }

    // ─────────────────────────────────────────
    //  POST /api/books
    // ─────────────────────────────────────────

    @Test
    @WithMockUser(username = "sahil12", password = "pass123")
    @DisplayName("POST /api/books — saves books, returns summary")
    void saveBooks_withAuth_returns200() throws Exception {
        AuthorDto authorDto = new AuthorDto();
        authorDto.setFirstName("George");
        authorDto.setLastName("Orwell");
        authorDto.setEmailId("george@books.com");

        BookRequestDto request = new BookRequestDto();
        request.setTitle("1984");
        request.setGenre("Fictional");
        request.setContent("A dystopian novel...");
        request.setAuthor(authorDto);

        SaveBooksResponse response = new SaveBooksResponse(1, 0, List.of());
        when(booksCRUD.saveBooks(anyList())).thenReturn(response);

        mockMvc.perform(post("/api/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saved").value(1))
                .andExpect(jsonPath("$.skipped").value(0));
    }

    @Test
    @DisplayName("POST /api/books — returns 401 without auth")
    void saveBooks_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────
    //  GET /api/books
    // ─────────────────────────────────────────

    @Test
    @DisplayName("GET /api/books — returns all books without auth")
    void getAllBooks_noAuth_returns200() throws Exception {
        when(booksCRUD.showBooks()).thenReturn(List.of(book));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("1984"))
                .andExpect(jsonPath("$[0].genre").value("Fictional"));
    }

    @Test
    @DisplayName("GET /api/books — returns empty list when no books")
    void getAllBooks_noBooks_returnsEmptyList() throws Exception {
        when(booksCRUD.showBooks()).thenReturn(List.of());

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ─────────────────────────────────────────
    //  GET /api/books/title/{title}
    // ─────────────────────────────────────────

    @Test
    @DisplayName("GET /api/books/title/{title} — returns books by title")
    void getByTitle_found_returns200() throws Exception {
        when(booksCRUD.getBooksByTitle("1984")).thenReturn(List.of(bookResponse));

        mockMvc.perform(get("/api/books/title/1984"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("1984"))
                .andExpect(jsonPath("$[0].authorName").value("George Orwell"));
    }

    // ─────────────────────────────────────────
    //  GET /api/books/genre/{genre}
    // ─────────────────────────────────────────

    @Test
    @DisplayName("GET /api/books/genre/{genre} — returns books by genre")
    void getByGenre_found_returns200() throws Exception {
        when(booksCRUD.getBooksByGenre("Fictional")).thenReturn(List.of(bookResponse));

        mockMvc.perform(get("/api/books/genre/Fictional"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].genre").value("Fictional"));
    }

    // ─────────────────────────────────────────
    //  PATCH /api/books/{id}
    // ─────────────────────────────────────────

    @Test
    @WithMockUser(username = "sahil12", password = "pass123")
    @DisplayName("PATCH /api/books/{id} — updates book with auth")
    void updateBook_withAuth_returns200() throws Exception {
        BookUpdateDto dto = new BookUpdateDto();
        dto.setTitle("Animal Farm");

        when(booksCRUD.updateBook(eq(1), any(BookUpdateDto.class))).thenReturn(book);

        mockMvc.perform(patch("/api/books/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/books/{id} — returns 401 without auth")
    void updateBook_withoutAuth_returns401() throws Exception {
        mockMvc.perform(patch("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────
    //  DELETE /api/books/{id}
    // ─────────────────────────────────────────

    @Test
    @WithMockUser(username = "sahil12", password = "pass123")
    @DisplayName("DELETE /api/books/{id} — deletes book with auth")
    void deleteBook_withAuth_returns200() throws Exception {
        when(booksCRUD.deleteBook(1)).thenReturn("Book with id 1 deleted successfully");

        mockMvc.perform(delete("/api/books/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Book with id 1 deleted successfully"));
    }

    @Test
    @DisplayName("DELETE /api/books/{id} — returns 401 without auth")
    void deleteBook_withoutAuth_returns401() throws Exception {
        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isUnauthorized());
    }
}
