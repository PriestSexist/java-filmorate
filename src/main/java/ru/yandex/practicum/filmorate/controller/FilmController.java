package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.InvalidIdentificatorException;
import ru.yandex.practicum.filmorate.exception.film.InvalidReleaseDateException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.HashMap;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    HashMap<Integer, Film> films = new HashMap<>();
    private static final LocalDate BIRTH_OF_CINEMA = LocalDate.of(1895, 12, 28);

    @PostMapping("/film")
    public HashMap<Integer, Film> postFilm(@Valid @RequestBody Film film) throws InvalidReleaseDateException, InvalidIdentificatorException {
        if (film.getReleaseDate().isAfter(BIRTH_OF_CINEMA) || film.getReleaseDate().equals(BIRTH_OF_CINEMA)) {
            if (!films.containsKey(film.getId())) {
                films.put(film.getId(), film);
            } else {
                throw new InvalidIdentificatorException("This id already exists");
            }
        } else {
            throw new InvalidReleaseDateException("Invalid release date");
        }
        return films;
    }

    @PutMapping("/{oldId}")
    public HashMap<Integer, Film> putFilm(@Valid @RequestBody Film film, @PathVariable int oldId) throws InvalidReleaseDateException {
        if (film.getReleaseDate().isAfter(BIRTH_OF_CINEMA) || film.getReleaseDate().equals(BIRTH_OF_CINEMA)) {
            films.remove(oldId);
            films.put(film.getId(), film);
        } else {
            throw new InvalidReleaseDateException("Invalid release date");
        }
        return films;
    }

    @GetMapping("/all")
    public HashMap<Integer, Film> getFilms() {
        return films;
    }
}
