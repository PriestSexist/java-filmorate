package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.InvalidIdentificatorException;
import ru.yandex.practicum.filmorate.exception.film.InvalidReleaseDateException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final AtomicInteger count = new AtomicInteger(0);
    private final HashMap<Integer, Film> films = new HashMap<>();
    private static final LocalDate BIRTH_OF_CINEMA = LocalDate.of(1895, 12, 28);

    @PostMapping()
    public Film postFilm(@Valid @RequestBody Film film) throws InvalidReleaseDateException {

        if (!film.getReleaseDate().isAfter(BIRTH_OF_CINEMA) && !film.getReleaseDate().equals(BIRTH_OF_CINEMA)) {
            throw new InvalidReleaseDateException("Invalid release date");
        }

        film.setId(count.incrementAndGet());
        films.put(film.getId(), film);

        return film;
    }

    @PutMapping()
    public Film putFilm(@Valid @RequestBody Film film) throws InvalidReleaseDateException, InvalidIdentificatorException {

        if (!film.getReleaseDate().isAfter(BIRTH_OF_CINEMA) && !film.getReleaseDate().equals(BIRTH_OF_CINEMA)) {
            throw new InvalidReleaseDateException("Invalid release date");
        }

        if (!films.containsKey(film.getId())) {
            throw new InvalidIdentificatorException("Invalid id");
        }

        films.replace(film.getId(), film);

        return film;
    }

    @GetMapping()
    public ArrayList<Film> getFilms() {
        return new ArrayList<>(films.values());
    }
}
