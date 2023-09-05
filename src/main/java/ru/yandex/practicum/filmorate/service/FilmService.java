package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage filmDbStorage;


    @Autowired
    public FilmService(FilmStorage filmDbStorage) {
        this.filmDbStorage = filmDbStorage;
    }

    public Optional<Film> postFilm(Film film) {
        if (film.getGenres() != null) {
            List<Genre> uniqGenres = film.getGenres().stream().distinct().collect(Collectors.toList());
            film.getGenres().clear();
            film.getGenres().addAll(uniqGenres);
        }
        return filmDbStorage.postFilm(film);
    }


    public Optional<Film> putFilm(Film film) {
        if (film.getGenres() != null) {
            List<Genre> uniqGenres = film.getGenres().stream().distinct().collect(Collectors.toList());
            film.getGenres().clear();
            film.getGenres().addAll(uniqGenres);
        }
        return filmDbStorage.putFilm(film);
    }

    public Collection<Film> getFilms() {
        return filmDbStorage.getFilms();
    }

    public Optional<Film> getFilmById(int filmId) {
        return filmDbStorage.getFilmById(filmId);
    }

    public Optional<Film> putLikeToFilm(int filmId, int userId) {
        return filmDbStorage.putLikeToFilm(filmId, userId);
    }

    public Optional<Film> deleteLikeToFilm(int filmId, int userId) {
        return filmDbStorage.deleteLikeFromFilm(filmId, userId);
    }

    public List<Film> getTopFilms(int count) {
        Comparator<Film> comparator = Comparator.comparing(film -> film.getLikes().size());
        List<Film> films = getFilms().stream()
                .sorted(comparator.reversed())
                .limit(count)
                .collect(Collectors.toList());
        for (Film film : films) {          // !
            if (film.getGenres() != null) {
                List<Genre> uniqGenres = film.getGenres().stream().distinct().collect(Collectors.toList());
                film.getGenres().clear();
                film.getGenres().addAll(uniqGenres);
            }
        }
        return films;
    }

    public void deleteFilm(int filmId) {
        filmDbStorage.deleteFilm(filmId);
    }
}
