package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Like;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Component("inMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {

    private AtomicInteger counterForFilms = new AtomicInteger(0);
    private AtomicInteger counterForLikes = new AtomicInteger(0);
    private final Collection<Film> films = new ArrayList<>();

    @Override
    public Optional<Film> postFilm(Film film) {
        film.setId(counterForFilms.incrementAndGet());
        films.add(film);
        return getFilmById(film.getId());
    }

    @Override
    public Optional<Film> putFilm(Film film) {
        Optional<Film> filmToRemove = getFilmById(film.getId());
        if (filmToRemove.isEmpty()) {
            return Optional.empty();
        }

        films.remove(filmToRemove.get());
        films.add(film);
        return getFilmById(film.getId());
    }

    @Override
    public Collection<Film> getFilms() {
        return films;
    }

    @Override
    public Optional<Film> getFilmById(int id) {
        return films.stream().filter(f -> f.getId() == id).findFirst();
    }

    @Override
    public Optional<Film> putLikeToFilm(int filmId, int userId) {
        if (getFilmById(filmId).isPresent()) {
            getFilmById(filmId).get().getLikes().add(new Like(counterForLikes.incrementAndGet(), filmId, userId));
        }
        return getFilmById(filmId);
    }

    @Override
    public Optional<Film> deleteLikeFromFilm(int filmId, int userId) {
        if (getFilmById(filmId).isPresent()) {
            getFilmById(filmId).get().getLikes().remove(new Like(counterForLikes.incrementAndGet(), filmId, userId));
        }
        return getFilmById(filmId);
    }

    @Override
    public Collection<Mpa> getMpas() {
        return null;
    }

    @Override
    public Optional<Genre> getGenreById(int id) {
        return Optional.empty();
    }

    @Override
    public Optional<Mpa> getMpaById(int id) {
        return Optional.empty();
    }

    @Override
    public Collection<Genre> getGenres() {
        return null;
    }

}
