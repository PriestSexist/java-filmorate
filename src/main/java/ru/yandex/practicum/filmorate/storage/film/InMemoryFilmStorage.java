package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Like;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryFilmStorage implements FilmStorage {

    private final AtomicInteger counterForFilms = new AtomicInteger(0);
    private final AtomicInteger counterForLikes = new AtomicInteger(0);
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
    public List<Integer> getFilmsIdByDirectorId(int id) {
        return null;
    }


    @Override
    public List<Film> searchByTitle(String query) {
        return null;
    }

    @Override
    public List<Film> searchByDirector(String query) {
        return null;
    }

    @Override
    public List<Film> searchByTitleByDirector(String query) {
        return null;
    }

    @Override
    public List<Film> getPopularByGenreByYear(int count, int genreId, int year) {
        return null;
    }

    public List<Film> getPopularByYear(int count, int year) {
        return null;
    }

    public List<Film> getPopularByGenre(int count, int genreId) {
        return null;
    }
}
