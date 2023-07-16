package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Map;

public interface FilmStorage {
    Film postFilm(Film film);

    Film putFilm(Film film);

    Map<Integer, Film> getFilms();

    Film getFilmById(int id);

    void clear();
}
