package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.*;
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


    public List<Film> getTopFilms(Map<String,String>allParams) {
        List<Film> topFilms = new ArrayList<>();
        int count;
        if (allParams.containsKey("count")){
            count = Integer.parseInt(allParams.get("count"));
        }else {
            count = 1;
        }
        if (!allParams.containsKey("year") && !allParams.containsKey("genreId")) {
            Comparator<Film> comparator = Comparator.comparing(film -> film.getLikes().size());
            topFilms = getFilms().stream()
                    .sorted(comparator.reversed())
                    .limit(count)
                    .collect(Collectors.toList());
        }
        else if (allParams.containsKey("genreId") && allParams.containsKey("year")){
            int genreId = Integer.parseInt(allParams.get("genreId"));
            int year = Integer.parseInt(allParams.get("year"));
            topFilms = filmDbStorage.getPopularByGenreByYear(count, genreId, year);
        }
        else if (allParams.containsKey("genreId")){
            int genreId = Integer.parseInt(allParams.get("genreId"));

            topFilms = filmDbStorage.getPopularByGenre(count, genreId);
        }
        else if (allParams.containsKey("year")){
            int year = Integer.parseInt(allParams.get("year"));
            topFilms = filmDbStorage.getPopularByYear(count, year);
        }
        return topFilms;
    }


}
