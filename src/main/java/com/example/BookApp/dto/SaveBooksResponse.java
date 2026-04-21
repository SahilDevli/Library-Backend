package com.example.BookApp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SaveBooksResponse {
    private int saved;            // number of books successfully saved
    private int skipped;          // number of books skipped (duplicates)
    private List<String> errors;  // entries that failed validation
}

