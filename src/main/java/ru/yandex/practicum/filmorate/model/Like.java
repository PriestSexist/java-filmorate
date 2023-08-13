package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Like {
    private final int likeId;
    private final int filmId;
    private final int userId;
}
