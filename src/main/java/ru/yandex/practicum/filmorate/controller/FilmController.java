package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.film.InvalidReleaseDateException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collection;
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
    public Collection<Film> getTopFilms(@RequestParam  Map<String, String> allParams){
        //    @RequestParam(required = false)
        //  @DateTimeFormat(pattern = "yyyy") LocalDate year ) {
        return filmService.getTopFilms(allParams);
    }


   /* @GetMapping("/popular")
    public Collection<Film> getTopFilms(@RequestParam(defaultValue = "10") int count,
                                        @RequestParam (required = false) int genreId,
                                        @RequestParam (required = false) int year){
                                    //    @RequestParam(required = false)
                                      //  @DateTimeFormat(pattern = "yyyy") LocalDate year ) {
        return filmService.getTopFilms(count, genreId, year);
    }*/

/*    @GetMapping("/popular")  //  GET /films/popular?count={limit}&genreId={genreId}&year={year}
    public Collection<Film> getPopularByGenreByYear(@RequestParam(defaultValue = "10") int count,
                                                    @RequestParam int genreId,
                                                    @DateTimeFormat(pattern = "yyyy") LocalDate year ) {
        return filmService.getPopularByGenreByYear(count, genreId, year);
    }*/


}
