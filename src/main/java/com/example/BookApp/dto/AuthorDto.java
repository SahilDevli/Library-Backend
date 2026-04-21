package com.example.BookApp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthorDto {
    private String firstName;   // required
    private String lastName;    // required
    private String emailId;     // required — used for duplicate check
    private String bio;
}

