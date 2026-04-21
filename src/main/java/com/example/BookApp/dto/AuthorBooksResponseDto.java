package com.example.BookApp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AuthorBooksResponseDto {
    private String firstName;
    private String lastName;
    private String bio;
    private List<BookSummaryDto> books;
}

