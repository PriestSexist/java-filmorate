package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.film.InvalidReleaseDateException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private static final LocalDate BIRTH_OF_CINEMA = LocalDate.of(1895, 12, 28);
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
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

        return filmService.putFilm(film).orElseThrow(() -> {
            throw new FilmNotFoundException("Film not found");
        });
    }

    @GetMapping
    public Collection<Film> getFilms() {
        return filmService.getFilms();
    }

    @GetMapping("/{filmId}")
    public Film getFilmById(@PathVariable int filmId) {
        return filmService.getFilmById(filmId).orElseThrow(() -> {
            throw new FilmNotFoundException("Film not found");
        });
    }

    @PutMapping("/{filmId}/like/{userId}")
    public Film putLikeToFilm(@PathVariable int filmId, @PathVariable int userId) {
        return filmService.putLikeToFilm(filmId, userId).orElseThrow(() -> {
            throw new NotFoundException("Film or User not found");
        });
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public Film deleteLikeToFilm(@PathVariable int filmId, @PathVariable int userId) {
        return filmService.deleteLikeToFilm(filmId, userId).orElseThrow(() -> {
            throw new NotFoundException("Film or User not found");
        });
    }

    @GetMapping("/popular")
    public Collection<Film> getTopFilms(@RequestParam Map<String, String> allParams) {
        return filmService.getTopFilms(allParams);
    }

    @GetMapping("/common")
    public Collection<Film> getCommonFilms(@RequestParam int userId, @RequestParam int friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirectorId(
            @PathVariable int directorId,
            @RequestParam(value = "sortBy", defaultValue = "likes") String sort) {
        // GET /films/director/{directorId}?sortBy=[year,likes]

        log.info("Вызван GET запрос для получения списка фильмов по режиссеру.");
        log.debug("Передан идентификатор режисера {},", directorId);


        return filmService.getFilmsByDirectorId(directorId, sort);
    }

    @GetMapping("/search")
    public List<Film> searchByTitleByDirector(@RequestParam String query,
                                              @RequestParam List<String> by) {
        return filmService.searchByTitleByDirector(query, by);
    }

}
