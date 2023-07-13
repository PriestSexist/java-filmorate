package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.HashSet;

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
    private final HashSet<Integer> friends = new HashSet<>();
}
