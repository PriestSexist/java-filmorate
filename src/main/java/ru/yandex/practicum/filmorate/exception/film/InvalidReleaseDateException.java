package ru.yandex.practicum.filmorate.exception.film;

public class InvalidReleaseDateException extends RuntimeException {

    public InvalidReleaseDateException(String message) {
        super(message);
    }
}
