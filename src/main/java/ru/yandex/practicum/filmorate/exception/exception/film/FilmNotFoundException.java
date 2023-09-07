package ru.yandex.practicum.filmorate.exception.exception.film;

public class FilmNotFoundException extends RuntimeException {
    public FilmNotFoundException(String message) {
        super(message);
    }
}
