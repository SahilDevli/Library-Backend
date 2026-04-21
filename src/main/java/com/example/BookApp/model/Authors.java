package com.example.BookApp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Authors {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int author_id;
    String firstName;
    String lastName;
    @Column(name = "author_emailId", unique = true, nullable = false)
    private String emailId;
    String bio;
    // Foreign Key Reference
    @JsonIgnore  // prevents infinite JSON loop: Authors → Books → Authors → ...
    @OneToMany(mappedBy = "author")
    private List<Books> books;

    @Override
    public String toString() {
        return "Authors{" +
                "author_id=" + author_id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", emailId='" + emailId + '\'' +
                ", bio='" + bio + '\'' +
                '}'; // books excluded — prevents StackOverflowError
    }
}
