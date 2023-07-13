package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.film.InvalidReleaseDateException;
import ru.yandex.practicum.filmorate.exception.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private static final LocalDate BIRTH_OF_CINEMA = LocalDate.of(1895, 12, 28);
    private final FilmService filmService;
    private final UserService userService;

    @Autowired
    public FilmController(FilmService filmService, UserService userService) {
        this.filmService = filmService;
        this.userService = userService;
    }

    @PostMapping()
    public Film postFilm(@Valid @RequestBody Film film) {

        if (!film.getReleaseDate().isAfter(BIRTH_OF_CINEMA) && !film.getReleaseDate().equals(BIRTH_OF_CINEMA)) {
            throw new InvalidReleaseDateException("Invalid release date");
        }

        return filmService.postFilm(film);
    }

    @PutMapping()
    public Film putFilm(@Valid @RequestBody Film film) {

        if (!film.getReleaseDate().isAfter(BIRTH_OF_CINEMA) && !film.getReleaseDate().equals(BIRTH_OF_CINEMA)) {
            throw new InvalidReleaseDateException("Invalid release date");
        }

        if (!filmService.getFilms().containsKey(film.getId())) {
            throw new FilmNotFoundException("Film not found");
        }

        return filmService.putFilm(film);
    }

    @GetMapping()
    public ArrayList<Film> getFilms() {
        return new ArrayList<>(filmService.getFilms().values());
    }

    @GetMapping("/{filmId}")
    public Film getFilmById(@PathVariable int filmId) {
        if (!filmService.getFilms().containsKey(filmId)) {
            throw new FilmNotFoundException("Film not found");
        }

        return filmService.getFilmById(filmId);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public Film putLikeToFilm(@PathVariable int filmId, @PathVariable int userId) {
        if (!filmService.getFilms().containsKey(filmId)) {
            throw new FilmNotFoundException("Film not found");
        }
        if (!userService.getUsers().containsKey(userId)) {
            throw new UserNotFoundException("User not found");
        }

        return filmService.putLikeToFilm(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public Film deleteLikeToFilm(@PathVariable int filmId, @PathVariable int userId) {
        if (!filmService.getFilms().containsKey(filmId)) {
            throw new FilmNotFoundException("Film not found");
        }
        if (!userService.getUsers().containsKey(userId)) {
            throw new UserNotFoundException("User not found");
        }

        return filmService.deleteLikeToFilm(filmId, userId);
    }

    @GetMapping("/popular")
    public ArrayList<Film> getTopFilms(@RequestParam(required = false, defaultValue = "10") int count) {
        return filmService.getTopFilms(count);
    }
}
