package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Builder
@AllArgsConstructor
public class Review {

    private int reviewId;
    @NotBlank
    private String content;
    @NotNull(message = "IsPositive cannot be empty or null")
    private Boolean isPositive; //использую класс-обертку, чтобы можно было проверить на null
    //@NotNull(message = "UserId cannot be null")
    @Positive(message = "UserId cannot be zero or negative")
    private final Integer userId;
   // @NotNull(message = "FilmId cannot be null")
    @Positive(message = "FilmId cannot be zero or negative")
    private final Integer filmId;
    private int useful;
}
