package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Like;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.utility.EventOperation;
import ru.yandex.practicum.filmorate.utility.EventType;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage filmDbStorage;
    private final DirectorStorage directorStorage;
    private final EventService eventService;


    @Autowired
    public FilmService(FilmStorage filmDbStorage, EventService eventService, DirectorStorage directorStorage) {
        this.filmDbStorage = filmDbStorage;
        this.eventService = eventService;
        this.directorStorage = directorStorage;
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

        return getFilmById(filmId)
                .map(film -> {
                    for (Like like : film.getLikes()) {
                        if (like.getUserId() == userId) {
                            return film;
                        }
                    }
                    return filmDbStorage.putLikeToFilm(filmId, userId).get();
                });
    }

    public Optional<Film> deleteLikeToFilm(int filmId, int userId) {
        if (userId <= 0) {
            throw new UserNotFoundException("Не может быть отрицательного id");
        }
        eventService.createEvent(userId, EventType.LIKE, EventOperation.REMOVE, filmId);
        return filmDbStorage.deleteLikeFromFilm(filmId, userId);
    }

    public List<Film> getTopFilms(Map<String, String> allParams) {
        List<Film> topFilms = new ArrayList<>();
        int count;
        if (allParams.containsKey("count")) {
            count = Integer.parseInt(allParams.get("count"));
        } else {
            count = 10;
        }
        if (!allParams.containsKey("year") && !allParams.containsKey("genreId")) {
            Comparator<Film> comparator = Comparator.comparing(film -> film.getLikes().size());
            topFilms = getFilms().stream()
                    .sorted(comparator.reversed())
                    .limit(count)
                    .collect(Collectors.toList());
            for (Film film : topFilms) {          // !
                if (film.getGenres() != null) {
                    List<Genre> uniqGenres = film.getGenres().stream().distinct().collect(Collectors.toList());
                    film.getGenres().clear();
                    film.getGenres().addAll(uniqGenres);
                }
            }
        } else if (allParams.containsKey("genreId") && allParams.containsKey("year")) {
            int genreId = Integer.parseInt(allParams.get("genreId"));
            int year = Integer.parseInt(allParams.get("year"));
            topFilms = filmDbStorage.getPopularByGenreByYear(count, genreId, year);
        } else if (allParams.containsKey("genreId")) {
            int genreId = Integer.parseInt(allParams.get("genreId"));
            topFilms = filmDbStorage.getPopularByGenre(count, genreId);
        } else if (allParams.containsKey("year")) {
            int year = Integer.parseInt(allParams.get("year"));
            topFilms = filmDbStorage.getPopularByYear(count, year);
        }
        return topFilms;
    }

    public Collection<Film> getCommonFilms(int userId, int friendId) {

        Comparator<Film> comparator = Comparator.comparing(film -> film.getLikes().size());

        return filmDbStorage.getFilms().stream()
                .filter(film -> containsId(film.getLikes(), userId) && containsId(film.getLikes(), friendId))
                .sorted(comparator.reversed())
                .collect(Collectors.toList());
    }

    private boolean containsId(final Set<Like> set, final int userId) {
        return set.stream().anyMatch(o -> o.getUserId() == userId);
    }

    public Film getFilm(int directorId) {
        return filmDbStorage.getFilmById(directorId).orElse(null);
    }

    public List<Film> getFilmsByDirectorId(int id, String sort) {
        if (!(sort.equals("likes") || sort.equals("year"))) {
            throw new IllegalArgumentException("неизвестная сортировка " + sort + ". Варианты: [likes, year]");
        }

        if (directorStorage.isDirectorPresent(id)) {
            List<Film> films = filmDbStorage.getFilmsIdByDirectorId(id).stream()
                    .map(this::getFilm)
                    .collect(Collectors.toList());

            if (sort.equals("likes")) {
                Comparator<Film> comparator = Comparator.comparing(film -> film.getLikes().size());
                return films.stream()
                        .sorted(comparator.reversed())
                        .collect(Collectors.toList());
            } else {
                return films.stream()
                        .sorted(Comparator.comparing(Film::getReleaseDate))
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    public Optional<Integer> deleteFilm(int filmId) {
        return filmDbStorage.deleteFilm(filmId);
    }

    public List<Film> searchByTitleByDirector(String query, List<String> by) {
        List<Film> searchFilms = new ArrayList<>();
        if (by.contains("title") && by.contains("director")) {
            searchFilms = filmDbStorage.searchByTitleByDirector(query);
        } else if (by.contains("title")) {
            searchFilms = filmDbStorage.searchByTitle(query);
        } else if (by.contains("director")) {
            searchFilms = filmDbStorage.searchByDirector(query);
        }

        Comparator<Film> comparator = Comparator.comparing(film -> film.getLikes().size());

        return searchFilms.stream()
                .sorted(comparator.reversed())
                .collect(Collectors.toList());
    }
}
