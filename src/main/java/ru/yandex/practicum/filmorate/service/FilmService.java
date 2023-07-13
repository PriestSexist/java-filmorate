package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private AtomicInteger counter = new AtomicInteger(0);
    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film postFilm(Film film) {
        film.setId(counter.incrementAndGet());
        return filmStorage.postFilm(film);
    }

    public Film putFilm(Film film) {
        return filmStorage.putFilm(film);
    }

    public HashMap<Integer, Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(int filmId) {
        return filmStorage.getFilmById(filmId);
    }

    public Film putLikeToFilm(int filmId, int userId) {
        Film film = filmStorage.getFilmById(filmId);
        film.getPeopleLiked().add(userId);
        return film;
    }

    public Film deleteLikeToFilm(int filmId, int userId) {
        Film film = filmStorage.getFilmById(filmId);
        film.getPeopleLiked().remove(userId);
        return film;
    }

    public ArrayList<Film> getTopFilms(int count) {
        Comparator<Film> comparator = Comparator.comparing(e -> e.getPeopleLiked().size());
        return (ArrayList<Film>) getFilms().values().stream()
                .sorted(comparator.reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public void clear() {
        filmStorage.getFilms().clear();
        counter = new AtomicInteger(0);
    }


}
