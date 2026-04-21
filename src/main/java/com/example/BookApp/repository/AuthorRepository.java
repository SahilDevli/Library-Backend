package com.example.BookApp.repository;

import com.example.BookApp.model.Authors;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<@NotNull Authors, @NotNull Integer> {

    // Check if author already exists by email
    @Query("SELECT a.author_id FROM Authors a WHERE a.emailId = ?1")
    Integer checkDuplicateInDB(String emailId);

    // Search by firstName or lastName (case-insensitive, partial match)
    @Query("SELECT a FROM Authors a WHERE LOWER(a.firstName) LIKE LOWER(CONCAT('%', ?1, '%')) OR LOWER(a.lastName) LIKE LOWER(CONCAT('%', ?1, '%'))")
    List<Authors> findByNameContaining(String name);
}
