package com.example.BookApp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookUpdateDto {
    private String title;       // null = don't update
    private Integer authorId;   // null = don't update
    private String genre;       // null = don't update
    private String content;     // null = don't update
}

