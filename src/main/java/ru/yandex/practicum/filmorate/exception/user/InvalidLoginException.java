package ru.yandex.practicum.filmorate.exception.user;

public class InvalidLoginException extends Exception {

    public InvalidLoginException(String message) {
        super(message);
    }

    public String getDetailMessage() {
        return getMessage();
    }
}
