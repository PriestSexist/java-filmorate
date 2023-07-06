package ru.yandex.practicum.filmorate.exception.film;

public class InvalidReleaseDateException extends Exception {

    public InvalidReleaseDateException(String message) {
        super(message);
    }

    public String getDetailMessage() {
        return getMessage();
    }
}
