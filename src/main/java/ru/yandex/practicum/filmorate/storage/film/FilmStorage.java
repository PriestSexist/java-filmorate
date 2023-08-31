package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Optional<Film> postFilm(Film film);

    Optional<Film> putFilm(Film film);

    Collection<Film> getFilms();

    Optional<Film> getFilmById(int id);

    Optional<Film> putLikeToFilm(int filmId, int userId);

    Optional<Film> deleteLikeFromFilm(int filmId, int userId);

    List<Film> getPopularByGenreByYear (int count, int genreId, int year);
    List<Film> getPopularByYear (int count, int year);
    List<Film> getPopularByGenre (int count, int genreId);
}
