package com.example.BookApp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Books {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int book_id;
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "author_id")  // FOREIGN KEY
    private Authors author;
    String title;
    String genre; // fictional, fantasy, romance, mystory, non-fictional

    @Column(columnDefinition = "TEXT")  // TEXT type in PostgreSQL — no LOB stream issue
    String book_content;

    @Override
    public String toString() {
        return "Books{" +
                "book_id=" + book_id +
                ", author=" + author +
                ", title='" + title + '\'' +
                ", genre='" + genre + '\'' +
                ", book_content='" + book_content + '\'' +
                '}';
    }
}