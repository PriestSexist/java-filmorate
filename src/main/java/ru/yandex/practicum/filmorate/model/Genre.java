package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Genre {
    private final int id;
    private String name;

    // Я сделал это отдельно, чтобы hashset правильно сортировал по хеш коду в хеш сете. Три сет не получается десеариализовать из json,
    // без отдельного десериализатора, а hashset сортирует по хеш коду, так что я решил изменить способ образования этого хеш кода.
    // Идентификатор жанра - уникальный набор циверок, который определяет жанр, так что, я просто вернул его.
    // Не знаю, на сколько правильно данное решение
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Genre)) return false;
        Genre genre = (Genre) o;
        return getId() == genre.getId();
    }

    @Override
    public int hashCode() {
        return getId();
    }
}
