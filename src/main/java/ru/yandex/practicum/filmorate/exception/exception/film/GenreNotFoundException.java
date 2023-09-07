package ru.yandex.practicum.filmorate.exception.exception.film;

public class GenreNotFoundException extends RuntimeException {
    public GenreNotFoundException(String message) {
        super(message);
    }
}
