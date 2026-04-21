package com.example.BookApp.service;

import com.example.BookApp.dto.*;
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
@DisplayName("BooksCRUD Service Tests")
class BooksCRUDTest {

    @Mock private BookRepository bRepo;
    @Mock private AuthorRepository aRepo;
    @InjectMocks private BooksCRUD booksCRUD;

    private Authors author;
    private Books book;
    private BookRequestDto bookRequest;
    private AuthorDto authorDto;

    @BeforeEach
    void setUp() {
        author = new Authors();
        author.setAuthor_id(1);
        author.setFirstName("George");
        author.setLastName("Orwell");
        author.setEmailId("george@books.com");
        author.setBio("English novelist");

        book = new Books();
        book.setBook_id(1);
        book.setTitle("1984");
        book.setGenre("Fictional");
        book.setBook_content("A dystopian novel...");
        book.setAuthor(author);

        authorDto = new AuthorDto();
        authorDto.setFirstName("George");
        authorDto.setLastName("Orwell");
        authorDto.setEmailId("george@books.com");
        authorDto.setBio("English novelist");

        bookRequest = new BookRequestDto();
        bookRequest.setTitle("1984");
        bookRequest.setGenre("Fictional");
        bookRequest.setContent("A dystopian novel...");
        bookRequest.setAuthor(authorDto);
    }

    // ─────────────────────────────────────────
    //  saveBooks
    // ─────────────────────────────────────────

    @Test
    @DisplayName("saveBooks — saves new book with new author successfully")
    void saveBooks_newBookNewAuthor_savedSuccessfully() {
        when(aRepo.checkDuplicateInDB("george@books.com")).thenReturn(null);
        when(aRepo.save(any(Authors.class))).thenReturn(author);
        when(bRepo.checkDuplicateInDB(anyString(), anyString())).thenReturn(null);
        when(bRepo.save(any(Books.class))).thenReturn(book);

        SaveBooksResponse response = booksCRUD.saveBooks(List.of(bookRequest));

        assertThat(response.getSaved()).isEqualTo(1);
        assertThat(response.getSkipped()).isEqualTo(0);
        assertThat(response.getErrors()).isEmpty();
        verify(bRepo, times(1)).save(any(Books.class));
    }

    @Test
    @DisplayName("saveBooks — reuses existing author when email matches")
    void saveBooks_existingAuthor_reused() {
        when(aRepo.checkDuplicateInDB("george@books.com")).thenReturn(1);
        when(aRepo.findById(1)).thenReturn(Optional.of(author));
        when(bRepo.checkDuplicateInDB(anyString(), anyString())).thenReturn(null);
        when(bRepo.save(any(Books.class))).thenReturn(book);

        SaveBooksResponse response = booksCRUD.saveBooks(List.of(bookRequest));

        assertThat(response.getSaved()).isEqualTo(1);
        verify(aRepo, never()).save(any(Authors.class)); // author not created again
    }

    @Test
    @DisplayName("saveBooks — skips duplicate book")
    void saveBooks_duplicateBook_skipped() {
        when(aRepo.checkDuplicateInDB("george@books.com")).thenReturn(1);
        when(aRepo.findById(1)).thenReturn(Optional.of(author));
        when(bRepo.checkDuplicateInDB(anyString(), anyString())).thenReturn(1); // already exists

        SaveBooksResponse response = booksCRUD.saveBooks(List.of(bookRequest));

        assertThat(response.getSaved()).isEqualTo(0);
        assertThat(response.getSkipped()).isEqualTo(1);
        verify(bRepo, never()).save(any(Books.class));
    }

    @Test
    @DisplayName("saveBooks — skips null entry and records error")
    void saveBooks_nullEntry_skippedWithError() {
        BookRequestDto nullRequest = new BookRequestDto(); // title is null

        SaveBooksResponse response = booksCRUD.saveBooks(List.of(nullRequest));

        assertThat(response.getSaved()).isEqualTo(0);
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0)).contains("Entry [0] skipped");
    }

    @Test
    @DisplayName("saveBooks — throws when list is null")
    void saveBooks_nullList_throwsNullPointerException() {
        assertThatThrownBy(() -> booksCRUD.saveBooks(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("request list must not be null");
    }

    @Test
    @DisplayName("saveBooks — handles mixed valid and invalid entries")
    void saveBooks_mixedEntries_savedAndErrors() {
        BookRequestDto badRequest = new BookRequestDto(); // missing title

        when(aRepo.checkDuplicateInDB("george@books.com")).thenReturn(null);
        when(aRepo.save(any(Authors.class))).thenReturn(author);
        when(bRepo.checkDuplicateInDB(anyString(), anyString())).thenReturn(null);
        when(bRepo.save(any(Books.class))).thenReturn(book);

        SaveBooksResponse response = booksCRUD.saveBooks(List.of(bookRequest, badRequest));

        assertThat(response.getSaved()).isEqualTo(1);
        assertThat(response.getErrors()).hasSize(1);
    }

    // ─────────────────────────────────────────
    //  showBooks
    // ─────────────────────────────────────────

    @Test
    @DisplayName("showBooks — returns all books")
    void showBooks_returnsAllBooks() {
        when(bRepo.showAllBooks()).thenReturn(List.of(book));

        List<Books> result = booksCRUD.showBooks();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("1984");
    }

    @Test
    @DisplayName("showBooks — returns empty list when no books")
    void showBooks_noBooksInDb_returnsEmptyList() {
        when(bRepo.showAllBooks()).thenReturn(List.of());

        List<Books> result = booksCRUD.showBooks();

        assertThat(result).isEmpty();
    }

    // ─────────────────────────────────────────
    //  getBooksByTitle
    // ─────────────────────────────────────────

    @Test
    @DisplayName("getBooksByTitle — returns books matching title")
    void getBooksByTitle_found_returnsBooks() {
        when(bRepo.findByTitle("1984")).thenReturn(List.of(book));

        List<BookResponseDto> result = booksCRUD.getBooksByTitle("1984");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("1984");
        assertThat(result.get(0).getAuthorName()).isEqualTo("George Orwell");
    }

    @Test
    @DisplayName("getBooksByTitle — throws when title not found")
    void getBooksByTitle_notFound_throwsResourceNotFoundException() {
        when(bRepo.findByTitle("Unknown")).thenReturn(List.of());

        assertThatThrownBy(() -> booksCRUD.getBooksByTitle("Unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No books found with title: Unknown");
    }

    @Test
    @DisplayName("getBooksByTitle — throws when title is null")
    void getBooksByTitle_nullTitle_throwsNullPointerException() {
        assertThatThrownBy(() -> booksCRUD.getBooksByTitle(null))
                .isInstanceOf(NullPointerException.class);
    }

    // ─────────────────────────────────────────
    //  getBooksByGenre
    // ─────────────────────────────────────────

    @Test
    @DisplayName("getBooksByGenre — returns books matching genre")
    void getBooksByGenre_found_returnsBooks() {
        when(bRepo.findByGenre("Fictional")).thenReturn(List.of(book));

        List<BookResponseDto> result = booksCRUD.getBooksByGenre("Fictional");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGenre()).isEqualTo("Fictional");
    }

    @Test
    @DisplayName("getBooksByGenre — throws when genre not found")
    void getBooksByGenre_notFound_throwsResourceNotFoundException() {
        when(bRepo.findByGenre("Unknown")).thenReturn(List.of());

        assertThatThrownBy(() -> booksCRUD.getBooksByGenre("Unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No books found with genre: Unknown");
    }

    // ─────────────────────────────────────────
    //  updateBook
    // ─────────────────────────────────────────

    @Test
    @DisplayName("updateBook — updates title and genre successfully")
    void updateBook_validId_updatesBook() {
        BookUpdateDto dto = new BookUpdateDto();
        dto.setTitle("Animal Farm");
        dto.setGenre("Satire");

        when(bRepo.findById(1)).thenReturn(Optional.of(book));
        when(bRepo.save(any(Books.class))).thenReturn(book);

        Books result = booksCRUD.updateBook(1, dto);

        assertThat(result).isNotNull();
        verify(bRepo).save(book);
    }

    @Test
    @DisplayName("updateBook — updates author when authorId provided")
    void updateBook_withAuthorId_updatesAuthor() {
        Authors newAuthor = new Authors();
        newAuthor.setAuthor_id(2);
        newAuthor.setFirstName("J.K.");
        newAuthor.setLastName("Rowling");

        BookUpdateDto dto = new BookUpdateDto();
        dto.setAuthorId(2);

        when(bRepo.findById(1)).thenReturn(Optional.of(book));
        when(aRepo.findById(2)).thenReturn(Optional.of(newAuthor));
        when(bRepo.save(any(Books.class))).thenReturn(book);

        booksCRUD.updateBook(1, dto);

        assertThat(book.getAuthor()).isEqualTo(newAuthor);
    }

    @Test
    @DisplayName("updateBook — throws when book id not found")
    void updateBook_invalidId_throwsResourceNotFoundException() {
        when(bRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> booksCRUD.updateBook(99, new BookUpdateDto()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book not found with id: 99");
    }

    @Test
    @DisplayName("updateBook — throws when authorId in dto does not exist")
    void updateBook_invalidAuthorId_throwsResourceNotFoundException() {
        BookUpdateDto dto = new BookUpdateDto();
        dto.setAuthorId(99);

        when(bRepo.findById(1)).thenReturn(Optional.of(book));
        when(aRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> booksCRUD.updateBook(1, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Author not found with id: 99");
    }

    // ─────────────────────────────────────────
    //  deleteBook
    // ─────────────────────────────────────────

    @Test
    @DisplayName("deleteBook — deletes book successfully")
    void deleteBook_validId_deletesBook() {
        when(bRepo.existsById(1)).thenReturn(true);

        String result = booksCRUD.deleteBook(1);

        assertThat(result).contains("deleted successfully");
        verify(bRepo).deleteById(1);
    }

    @Test
    @DisplayName("deleteBook — throws when book not found")
    void deleteBook_invalidId_throwsResourceNotFoundException() {
        when(bRepo.existsById(99)).thenReturn(false);

        assertThatThrownBy(() -> booksCRUD.deleteBook(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book not found with id: 99");
        verify(bRepo, never()).deleteById(anyInt());
    }
}

