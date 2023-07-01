package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class User {
    private int id;
    @Email(message = "Email should be valid")
    private String email;
    @NotBlank(message = "Login can't be blank")
    private String login;
    private String name;
    @PastOrPresent(message = "Birthday should be before current time")
    private LocalDate birthday;
}
