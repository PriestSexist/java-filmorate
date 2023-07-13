package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final HashMap<Integer, Film> films = new HashMap<>();

    @Override
    public Film postFilm(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film putFilm(Film film) {
        films.replace(film.getId(), film);
        return film;
    }

    @Override
    public HashMap<Integer, Film> getFilms() {
        return films;
    }

    @Override
    public Film getFilmById(int id) {
        return films.get(id);
    }
}
