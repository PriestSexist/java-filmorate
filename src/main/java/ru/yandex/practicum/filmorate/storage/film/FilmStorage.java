package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

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

    List<Integer> getFilmsIdByDirectorId(int id);

    List<Integer> getDirectorsIdByFilmId(int filmId);

    void deleteFilmDirectors(int filmId);

    void addDirectorToFilm(Integer filmId, Integer directorId);

    void setFilmDirectors(List<Director> directorsId, int filmId);

    void updateFilmDirectors(List<Director> directorsId, int filmId);
}
