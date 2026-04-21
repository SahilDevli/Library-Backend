package com.example.BookApp.controller;

import com.example.BookApp.config.SecurityConfig;
import com.example.BookApp.dto.AuthorBooksResponseDto;
import com.example.BookApp.dto.AuthorUpdateDto;
import com.example.BookApp.dto.BookSummaryDto;
import com.example.BookApp.exception.ResourceNotFoundException;
import com.example.BookApp.model.Authors;
import com.example.BookApp.service.AuthorsCRUD;
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

@WebMvcTest(AuthorsController.class)
@Import(SecurityConfig.class)
@DisplayName("AuthorsController API Tests")
class AuthorsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean AuthorsCRUD authorsCRUD;

    private Authors author;
    private AuthorBooksResponseDto authorResponse;

    @BeforeEach
    void setUp() {
        author = new Authors();
        author.setAuthor_id(1);
        author.setFirstName("George");
        author.setLastName("Orwell");
        author.setEmailId("george@books.com");
        author.setBio("English novelist");

        authorResponse = new AuthorBooksResponseDto(
                "George", "Orwell", "English novelist",
                List.of(new BookSummaryDto("1984", "Fictional"))
        );
    }

    // ─────────────────────────────────────────
    //  GET /api/authors
    // ─────────────────────────────────────────

    @Test
    @DisplayName("GET /api/authors — returns all authors without auth")
    void getAllAuthors_noAuth_returns200() throws Exception {
        when(authorsCRUD.getAuthors()).thenReturn(List.of(author));

        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("George"))
                .andExpect(jsonPath("$[0].lastName").value("Orwell"));
    }

    @Test
    @DisplayName("GET /api/authors — returns empty list when no authors")
    void getAllAuthors_noAuthors_returnsEmptyList() throws Exception {
        when(authorsCRUD.getAuthors()).thenReturn(List.of());

        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ─────────────────────────────────────────
    //  GET /api/authors/search?name=
    // ─────────────────────────────────────────

    @Test
    @DisplayName("GET /api/authors/search?name= — returns matching authors")
    void searchByName_found_returns200() throws Exception {
        when(authorsCRUD.getAuthorsByName("George")).thenReturn(List.of(authorResponse));

        mockMvc.perform(get("/api/authors/search").param("name", "George"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("George"))
                .andExpect(jsonPath("$[0].books[0].title").value("1984"));
    }

    @Test
    @DisplayName("GET /api/authors/search?name= — returns 404 when not found")
    void searchByName_notFound_returns404() throws Exception {
        when(authorsCRUD.getAuthorsByName("Unknown"))
                .thenThrow(new ResourceNotFoundException("No authors found with name: Unknown"));

        mockMvc.perform(get("/api/authors/search").param("name", "Unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No authors found with name: Unknown"));
    }

    @Test
    @DisplayName("GET /api/authors/search — returns 400 when name param missing")
    void searchByName_missingParam_returns400() throws Exception {
        mockMvc.perform(get("/api/authors/search"))
                .andExpect(status().isBadRequest());
    }

    // ─────────────────────────────────────────
    //  PATCH /api/authors/{id}
    // ─────────────────────────────────────────

    @Test
    @WithMockUser(username = "sahil12", password = "pass123")
    @DisplayName("PATCH /api/authors/{id} — updates author with auth")
    void updateAuthor_withAuth_returns200() throws Exception {
        AuthorUpdateDto dto = new AuthorUpdateDto();
        dto.setFirstName("Eric");
        dto.setBio("Updated bio");

        when(authorsCRUD.updateAuthor(eq(1), any(AuthorUpdateDto.class))).thenReturn(author);

        mockMvc.perform(patch("/api/authors/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/authors/{id} — returns 401 without auth")
    void updateAuthor_withoutAuth_returns401() throws Exception {
        mockMvc.perform(patch("/api/authors/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "sahil12", password = "pass123")
    @DisplayName("PATCH /api/authors/{id} — returns 404 when author not found")
    void updateAuthor_notFound_returns404() throws Exception {
        when(authorsCRUD.updateAuthor(eq(99), any(AuthorUpdateDto.class)))
                .thenThrow(new ResourceNotFoundException("Author not found with id: 99"));

        mockMvc.perform(patch("/api/authors/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Author not found with id: 99"));
    }

    // ─────────────────────────────────────────
    //  DELETE /api/authors/{id}
    // ─────────────────────────────────────────

    @Test
    @WithMockUser(username = "sahil12", password = "pass123")
    @DisplayName("DELETE /api/authors/{id} — deletes author with auth")
    void deleteAuthor_withAuth_returns200() throws Exception {
        when(authorsCRUD.deleteAuthor(1)).thenReturn("Author with id 1 deleted successfully");

        mockMvc.perform(delete("/api/authors/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Author with id 1 deleted successfully"));
    }

    @Test
    @DisplayName("DELETE /api/authors/{id} — returns 401 without auth")
    void deleteAuthor_withoutAuth_returns401() throws Exception {
        mockMvc.perform(delete("/api/authors/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "sahil12", password = "pass123")
    @DisplayName("DELETE /api/authors/{id} — returns 400 when author has books")
    void deleteAuthor_hasBooks_returns400() throws Exception {
        when(authorsCRUD.deleteAuthor(1))
                .thenThrow(new IllegalArgumentException("Cannot delete author with id 1 — they have 3 book(s). Delete the books first."));

        mockMvc.perform(delete("/api/authors/1").with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Cannot delete author")));
    }

    @Test
    @WithMockUser(username = "sahil12", password = "pass123")
    @DisplayName("DELETE /api/authors/{id} — returns 404 when author not found")
    void deleteAuthor_notFound_returns404() throws Exception {
        when(authorsCRUD.deleteAuthor(99))
                .thenThrow(new ResourceNotFoundException("Author not found with id: 99"));

        mockMvc.perform(delete("/api/authors/99").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Author not found with id: 99"));
    }
}
