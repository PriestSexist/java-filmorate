package ru.yandex.practicum.filmorate.exception.exception.user;

public class InvalidLoginException extends RuntimeException {

    public InvalidLoginException(String message) {
        super(message);
    }
}
