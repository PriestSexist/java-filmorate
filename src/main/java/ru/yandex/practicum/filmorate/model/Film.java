package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class Film {
    private int id;
    @NotBlank(message = "Name can't be blank")
    private String name;
    @Size(max = 200, message = "description should be 200 symbols or less")
    private String description;
    private LocalDate releaseDate;
    @Positive(message = "Duration can't be zero or negative")
    private int duration; // В минутах
    private Mpa mpa;
    private List<Director> directors;
    private final ArrayList<Genre> genres = new ArrayList<>();
    private final HashSet<Like> likes = new HashSet<>();
}
