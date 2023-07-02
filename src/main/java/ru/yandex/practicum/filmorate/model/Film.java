package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@Builder
public class Film {
    private int id;
    @NotBlank(message = "Name can't be blank")
    private String name;
    private LocalDate releaseDate;
    @Positive(message = "Duration can't be zero or negative")
    private int duration; // В минутах
    @Size(max = 200, message = "description should be 200 symbols or less")
    private String description;
}
