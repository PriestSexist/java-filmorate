package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage filmDbStorage;
    private final DirectorStorage directorStorage;

    @Autowired
    public FilmService(FilmStorage filmDbStorage, DirectorStorage directorStorage) {
        this.filmDbStorage = filmDbStorage;
        this.directorStorage = directorStorage;
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
        return getFilms().stream()
                .sorted(comparator.reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public Film getFilm(int directorId) {
        return filmDbStorage.getFilmById(directorId).orElse(null);
    }

    public List<Film> getFilmsByDirectorId(int id, String sort) {
        if (!(sort.equals("likes") || sort.equals("year"))) {
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


    public List<Film> searchByTitleByDirector(String query, List<String> by) {
        List<Film> searchFimls = new ArrayList<>();
        if (by.contains("title") && by.contains("director")) {
            searchFimls = filmDbStorage.searchByTitleByDirector(query);
        } else if (by.contains("title")) {
            searchFimls = filmDbStorage.searchByTitle(query);
        } else if (by.contains("director")) {
            searchFimls = filmDbStorage.searchByDirector(query);
        }
        return searchFimls;
    }
}
