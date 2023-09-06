package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.utility.EventOperation;
import ru.yandex.practicum.filmorate.utility.EventType;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage filmDbStorage;

    private final EventService eventService;

    @Autowired
    public FilmService(FilmStorage filmDbStorage, EventService eventService) {
        this.filmDbStorage = filmDbStorage;
        this.eventService = eventService;
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
        eventService.createEvent(userId, EventType.LIKE, EventOperation.ADD, filmId);
        return filmDbStorage.putLikeToFilm(filmId, userId);
    }

    public Optional<Film> deleteLikeToFilm(int filmId, int userId) {
        if (userId < 0) {
            throw new NotFoundException("Не может быть отрицательного id");
        }
        eventService.createEvent(userId, EventType.LIKE, EventOperation.REMOVE, filmId);
        return filmDbStorage.deleteLikeFromFilm(filmId, userId);
    }

    public List<Film> getTopFilms(int count) {
        Comparator<Film> comparator = Comparator.comparing(film -> film.getLikes().size());
        return getFilms().stream()
                .sorted(comparator.reversed())
                .limit(count)
                .collect(Collectors.toList());
    }
}
