package com.example.BookApp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookRequestDto {
    private String title;       // required
    private String genre;
    private String content;
    private AuthorDto author;   // required — nested author object
}

