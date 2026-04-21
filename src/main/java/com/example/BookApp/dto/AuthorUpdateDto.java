package com.example.BookApp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthorUpdateDto {
    private String firstName;   // null = don't update
    private String lastName;    // null = don't update
    private String emailId;     // null = don't update
    private String bio;         // null = don't update
}

