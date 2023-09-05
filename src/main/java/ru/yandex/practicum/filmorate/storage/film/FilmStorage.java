package ru.yandex.practicum.filmorate.storage.film;

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

    //Удалю при следующем push. Дублируется
    //   List<Integer> getDirectorsIdByFilmId(int filmId);

    List<Film> searchByTitle(String query);

    List<Film> searchByDirector(String query);

    List<Film> searchByTitleByDirector(String query);
}
