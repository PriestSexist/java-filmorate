package ru.yandex.practicum.filmorate.exception.film;

public class MpaNotFoundException extends RuntimeException {
    public MpaNotFoundException(String message) {
        super(message);
    }
}
