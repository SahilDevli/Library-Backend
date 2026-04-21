package com.example.BookApp.service;

import com.example.BookApp.dto.AuthorBooksResponseDto;
import com.example.BookApp.dto.AuthorUpdateDto;
import com.example.BookApp.exception.ResourceNotFoundException;
import com.example.BookApp.model.Authors;
import com.example.BookApp.model.Books;
import com.example.BookApp.repository.AuthorRepository;
import com.example.BookApp.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorsCRUD Service Tests")
class AuthorsCRUDTest {

    @Mock private AuthorRepository aRepo;
    @Mock private BookRepository bRepo;
    @InjectMocks private AuthorsCRUD authorsCRUD;

    private Authors author;

    @BeforeEach
    void setUp() {
        author = new Authors();
        author.setAuthor_id(1);
        author.setFirstName("George");
        author.setLastName("Orwell");
        author.setEmailId("george@books.com");
        author.setBio("English novelist");
    }

    // ─────────────────────────────────────────
    //  saveAuthor
    // ─────────────────────────────────────────

    @Test
    @DisplayName("saveAuthor — saves new author successfully")
    void saveAuthor_newAuthor_returnsTrue() {
        when(aRepo.checkDuplicateInDB("george@books.com")).thenReturn(null);
        when(aRepo.save(any(Authors.class))).thenReturn(author);

        Boolean result = authorsCRUD.saveAuthor(author);

        assertThat(result).isTrue();
        verify(aRepo).save(author);
    }

    @Test
    @DisplayName("saveAuthor — returns false when author already exists")
    void saveAuthor_duplicateEmail_returnsFalse() {
        when(aRepo.checkDuplicateInDB("george@books.com")).thenReturn(1);

        Boolean result = authorsCRUD.saveAuthor(author);

        assertThat(result).isFalse();
        verify(aRepo, never()).save(any(Authors.class));
    }

    @Test
    @DisplayName("saveAuthor — throws when author is null")
    void saveAuthor_nullAuthor_throwsNullPointerException() {
        assertThatThrownBy(() -> authorsCRUD.saveAuthor(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("author must not be null");
    }

    @Test
    @DisplayName("saveAuthor — throws when emailId is null")
    void saveAuthor_nullEmail_throwsNullPointerException() {
        author.setEmailId(null);

        assertThatThrownBy(() -> authorsCRUD.saveAuthor(author))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("emailId must not be null");
    }

    // ─────────────────────────────────────────
    //  getAuthors
    // ─────────────────────────────────────────

    @Test
    @DisplayName("getAuthors — returns all authors")
    void getAuthors_returnsAllAuthors() {
        when(aRepo.findAll()).thenReturn(List.of(author));

        List<Authors> result = authorsCRUD.getAuthors();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("George");
    }

    @Test
    @DisplayName("getAuthors — returns empty list when no authors")
    void getAuthors_noAuthors_returnsEmptyList() {
        when(aRepo.findAll()).thenReturn(List.of());

        List<Authors> result = authorsCRUD.getAuthors();

        assertThat(result).isEmpty();
    }

    // ─────────────────────────────────────────
    //  getAuthorsByName
    // ─────────────────────────────────────────

    @Test
    @DisplayName("getAuthorsByName — returns matching authors with books")
    void getAuthorsByName_found_returnsAuthorResponse() {
        Books book = new Books();
        book.setTitle("1984");
        book.setGenre("Fictional");
        author.setBooks(List.of(book));

        when(aRepo.findByNameContaining("George")).thenReturn(List.of(author));

        List<AuthorBooksResponseDto> result = authorsCRUD.getAuthorsByName("George");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("George");
        assertThat(result.get(0).getBooks()).hasSize(1);
        assertThat(result.get(0).getBooks().get(0).getTitle()).isEqualTo("1984");
    }

    @Test
    @DisplayName("getAuthorsByName — throws when no authors found")
    void getAuthorsByName_notFound_throwsResourceNotFoundException() {
        when(aRepo.findByNameContaining("Unknown")).thenReturn(List.of());

        assertThatThrownBy(() -> authorsCRUD.getAuthorsByName("Unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No authors found with name: Unknown");
    }

    @Test
    @DisplayName("getAuthorsByName — throws when name is null")
    void getAuthorsByName_nullName_throwsNullPointerException() {
        assertThatThrownBy(() -> authorsCRUD.getAuthorsByName(null))
                .isInstanceOf(NullPointerException.class);
    }

    // ─────────────────────────────────────────
    //  updateAuthor
    // ─────────────────────────────────────────

    @Test
    @DisplayName("updateAuthor — updates firstName and lastName")
    void updateAuthor_validId_updatesFields() {
        AuthorUpdateDto dto = new AuthorUpdateDto();
        dto.setFirstName("Eric");
        dto.setLastName("Blair");

        when(aRepo.findById(1)).thenReturn(Optional.of(author));
        when(aRepo.save(any(Authors.class))).thenReturn(author);

        Authors result = authorsCRUD.updateAuthor(1, dto);

        assertThat(result).isNotNull();
        assertThat(author.getFirstName()).isEqualTo("Eric");
        assertThat(author.getLastName()).isEqualTo("Blair");
        verify(aRepo).save(author);
    }

    @Test
    @DisplayName("updateAuthor — null fields in dto are ignored")
    void updateAuthor_nullFieldsInDto_notOverwritten() {
        AuthorUpdateDto dto = new AuthorUpdateDto(); // all null

        when(aRepo.findById(1)).thenReturn(Optional.of(author));
        when(aRepo.save(any(Authors.class))).thenReturn(author);

        authorsCRUD.updateAuthor(1, dto);

        // Fields should remain unchanged
        assertThat(author.getFirstName()).isEqualTo("George");
        assertThat(author.getLastName()).isEqualTo("Orwell");
    }

    @Test
    @DisplayName("updateAuthor — throws when author id not found")
    void updateAuthor_invalidId_throwsResourceNotFoundException() {
        when(aRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorsCRUD.updateAuthor(99, new AuthorUpdateDto()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Author not found with id: 99");
    }

    // ─────────────────────────────────────────
    //  deleteAuthor
    // ─────────────────────────────────────────

    @Test
    @DisplayName("deleteAuthor — deletes author with no books")
    void deleteAuthor_noBooks_deletesSuccessfully() {
        when(aRepo.existsById(1)).thenReturn(true);
        when(bRepo.countByAuthorId(1)).thenReturn(0L);

        String result = authorsCRUD.deleteAuthor(1);

        assertThat(result).contains("deleted successfully");
        verify(aRepo).deleteById(1);
    }

    @Test
    @DisplayName("deleteAuthor — throws when author has books")
    void deleteAuthor_authorHasBooks_throwsIllegalArgumentException() {
        when(aRepo.existsById(1)).thenReturn(true);
        when(bRepo.countByAuthorId(1)).thenReturn(3L);

        assertThatThrownBy(() -> authorsCRUD.deleteAuthor(1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot delete author")
                .hasMessageContaining("3 book(s)");
        verify(aRepo, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("deleteAuthor — throws when author id not found")
    void deleteAuthor_invalidId_throwsResourceNotFoundException() {
        when(aRepo.existsById(99)).thenReturn(false);

        assertThatThrownBy(() -> authorsCRUD.deleteAuthor(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Author not found with id: 99");
        verify(aRepo, never()).deleteById(anyInt());
    }
}

