package ru.yandex.practicum.filmorate.exception;

public class InvalidIdentificatorException extends Exception {
    public InvalidIdentificatorException(String message) {
        super(message);
    }

    public String getDetailMessage() {
        return getMessage();
    }
}
