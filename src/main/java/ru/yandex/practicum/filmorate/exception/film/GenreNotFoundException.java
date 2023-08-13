package ru.yandex.practicum.filmorate.exception.film;

public class GenreNotFoundException extends RuntimeException {
    public GenreNotFoundException(String message) {
        super(message);
    }
}
