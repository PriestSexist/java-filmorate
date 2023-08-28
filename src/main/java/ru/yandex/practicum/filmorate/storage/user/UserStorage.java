package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

    Optional<User> postUser(User user);

    Optional<User> putUser(User user);

    Collection<User> getUsers();

    Optional<User> getUserById(int userId);

    Optional<User> putUserFriend(int userId, int friendId);

    Optional<User> deleteUserFriend(int userId, int friendId);

    Collection<User> getFriends(int userId);

    Collection<User> getCommonFriends(int userId, int otherId);
}
