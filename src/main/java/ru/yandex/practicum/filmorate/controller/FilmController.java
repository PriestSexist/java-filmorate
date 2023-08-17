package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.film.InvalidReleaseDateException;
import ru.yandex.practicum.filmorate.exception.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

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

    @PostMapping
    public Film postFilm(@Valid @RequestBody Film film) {

        if (!film.getReleaseDate().isAfter(BIRTH_OF_CINEMA) && !film.getReleaseDate().equals(BIRTH_OF_CINEMA)) {
            throw new InvalidReleaseDateException("Invalid release date");
        }

        return filmService.postFilm(film).get();
    }

    @PutMapping
    public Film putFilm(@Valid @RequestBody Film film) {

        if (!film.getReleaseDate().isAfter(BIRTH_OF_CINEMA) && !film.getReleaseDate().equals(BIRTH_OF_CINEMA)) {
            throw new InvalidReleaseDateException("Invalid release date");
        }

        Optional<Film> optionalFilm = filmService.getFilmById(film.getId());

        if (optionalFilm.isEmpty()) {
            throw new FilmNotFoundException("Film not found");
        }

        return filmService.putFilm(film).get();
    }

    @GetMapping
    public Collection<Film> getFilms() {
        return filmService.getFilms();
    }

    @GetMapping("/{filmId}")
    public Film getFilmById(@PathVariable int filmId) {

        Optional<Film> film = filmService.getFilmById(filmId);

        if (film.isEmpty()) {
            throw new FilmNotFoundException("Film not found");
        }

        return film.get();
    }

    @PutMapping("/{filmId}/like/{userId}")
    public Film putLikeToFilm(@PathVariable int filmId, @PathVariable int userId) {

        Optional<Film> filmOptional = filmService.getFilmById(filmId);
        Optional<User> userOptional = userService.getUserById(userId);

        if (filmOptional.isEmpty()) {
            throw new FilmNotFoundException("Film not found");
        }

        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }

        return filmService.putLikeToFilm(filmId, userId).get();
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public Film deleteLikeToFilm(@PathVariable int filmId, @PathVariable int userId) {

        Optional<Film> filmOptional = filmService.getFilmById(filmId);
        Optional<User> userOptional = userService.getUserById(userId);

        if (filmOptional.isEmpty()) {
            throw new FilmNotFoundException("Film not found");
        }

        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }

        return filmService.deleteLikeToFilm(filmId, userId).get();
    }

    @GetMapping("/popular")
    public Collection<Film> getTopFilms(@RequestParam(defaultValue = "10") int count) {
        return filmService.getTopFilms(count);
    }

}
