package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Optional<Film> postFilm(Film film);

    Optional<Film> putFilm(Film film);

    Collection<Film> getFilms();

    Optional<Film> getFilmById(int id);

    Optional<Film> putLikeToFilm(int filmId, int userId);

    Optional<Film> deleteLikeFromFilm(int filmId, int userId);

    Collection<Mpa> getMpas();

    Optional<Genre> getGenreById(int id);

    Optional<Mpa> getMpaById(int id);

    Collection<Genre> getGenres();
}
