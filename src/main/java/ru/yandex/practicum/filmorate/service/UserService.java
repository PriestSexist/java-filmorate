package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User postUser(User user) {

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        return userStorage.postUser(user);
    }

    public User putUser(User user) {

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        return userStorage.putUser(user);
    }

    public Map<Integer, User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUserById(int id) {
        return userStorage.getUserById(id);
    }

    public User putUserFriend(int id, int friendId) {

        User user = userStorage.getUserById(id);
        User friend = userStorage.getUserById(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(id);
        return user;
    }

    public User deleteUserFriend(int id, int friendId) {

        User user = userStorage.getUserById(id);
        User friend = userStorage.getUserById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);
        return user;
    }

    public List<User> getFriends(int id) {

        HashSet<Integer> friendsIds = userStorage.getUserById(id).getFriends();
        ArrayList<User> friends = new ArrayList<>();

        friendsIds.forEach(idFormSet -> friends.add(userStorage.getUserById(idFormSet)));

        return friends;
    }

    public List<User> getCommonFriends(int id, int otherId) {

        HashSet<Integer> myFriendsIds = new HashSet<>(userStorage.getUserById(id).getFriends());
        HashSet<Integer> otherFriendsIds = new HashSet<>(userStorage.getUserById(otherId).getFriends());
        List<User> commonFriends = new ArrayList<>();

        myFriendsIds.retainAll(otherFriendsIds);

        myFriendsIds.forEach(commonId -> commonFriends.add(userStorage.getUserById(commonId)));

        return commonFriends;
    }

}
