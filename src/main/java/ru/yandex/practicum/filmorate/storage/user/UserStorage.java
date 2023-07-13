package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;

public interface UserStorage {

    User postUser(User user);

    User putUser(User user);

    HashMap<Integer, User> getUsers();

    User getUserById(int id);
}
