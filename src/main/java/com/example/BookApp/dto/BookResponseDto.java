package com.example.BookApp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BookResponseDto {
    private String title;
    private String authorName;
    private String genre;
    private String content;
}

