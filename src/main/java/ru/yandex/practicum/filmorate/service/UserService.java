package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Optional;

@Service
public class UserService {

    private final UserStorage userDbStorage;

    @Autowired
    public UserService(UserStorage userDbStorage) {
        this.userDbStorage = userDbStorage;
    }

    public Optional<User> postUser(User user) {

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        return userDbStorage.postUser(user);
    }

    public Optional<User> putUser(User user) {

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        return userDbStorage.putUser(user);
    }

    public Collection<User> getUsers() {
        return userDbStorage.getUsers();
    }

    public Optional<User> getUserById(int id) {
        return userDbStorage.getUserById(id);
    }

    public Optional<User> putUserFriend(int id, int friendId) {
        return userDbStorage.putUserFriend(id, friendId);
    }

    public Optional<User> deleteUserFriend(int id, int friendId) {
        return userDbStorage.deleteUserFriend(id, friendId);
    }

    public Collection<User> getFriends(int id) {
        return userDbStorage.getFriends(id);
    }

    public Collection<User> getCommonFriends(int userId, int otherId) {
        return userDbStorage.getCommonFriends(userId, otherId);
    }

}
