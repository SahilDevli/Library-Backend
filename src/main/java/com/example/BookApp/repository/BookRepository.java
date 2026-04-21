package com.example.BookApp.repository;

import com.example.BookApp.model.Books;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<@NotNull Books, @NotNull Integer> {

    // Check if a book with same title and author already exists
    @Query("SELECT b.book_id FROM Books b JOIN b.author a WHERE b.title = ?1 AND CONCAT(a.firstName, a.lastName) = ?2")
    Integer checkDuplicateInDB(String title, String authorName);

    // Get all books with author details
    @Query("SELECT b FROM Books b JOIN FETCH b.author")
    List<Books> showAllBooks();

    // Get books by title
    @Query("SELECT b FROM Books b JOIN FETCH b.author WHERE b.title = ?1")
    List<Books> findByTitle(String title);

    // Get books by genre
    @Query("SELECT b FROM Books b JOIN FETCH b.author WHERE b.genre = ?1")
    List<Books> findByGenre(String genre);

    // Count books belonging to an author (used for safe author delete)
    @Query("SELECT COUNT(b) FROM Books b WHERE b.author.author_id = ?1")
    long countByAuthorId(int authorId);
}