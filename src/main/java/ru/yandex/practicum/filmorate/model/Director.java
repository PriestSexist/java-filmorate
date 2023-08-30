package ru.yandex.practicum.filmorate.model;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Director {
    /** Поле с уникальным идентификатором режисера */
    private int id;

    /** Поле с именем режисера */
    @NotBlank(message = "Имя не может быть пустым.")
    private String name;
}
