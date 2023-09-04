package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmDbStorage;
    private final DirectorStorage directorStorage;

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
        return getFilms().stream()
                .sorted(comparator.reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public Film getFilm(int directorId) {
        Optional<Film> o_film = filmDbStorage.getFilmById(directorId);
        return o_film.orElse(null);
    }

    public List<Film> getFilmsByDirectorId(int id, String sort) {
        if (!(sort.equals("likes") || sort.equals("year"))){
            throw new IllegalArgumentException("неизвестная сортировка " + sort + ". Варианты: [likes, year]");
        }

        if (directorStorage.isDirectorPresent(id)) {
            List<Film> films = filmDbStorage.getFilmsIdByDirectorId(id).stream()
                    .map(this::getFilm)
                    .collect(Collectors.toList());

            if (sort.equals("likes")) {
                Comparator<Film> comparator = Comparator.comparing(film -> film.getLikes().size());
                return films.stream()
                        .sorted(comparator.reversed())
                        .collect(Collectors.toList());
            } else {
                return films.stream()
                        .sorted(Comparator.comparing(Film::getReleaseDate))
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}
