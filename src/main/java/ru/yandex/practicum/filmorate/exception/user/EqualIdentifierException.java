package ru.yandex.practicum.filmorate.exception.user;

public class EqualIdentifierException extends RuntimeException {
    public EqualIdentifierException(String message) {
        super(message);
    }
}
