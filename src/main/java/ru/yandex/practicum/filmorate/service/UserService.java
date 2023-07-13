package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class UserService {

    private AtomicInteger counter = new AtomicInteger(0);
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User postUser(User user) {

        user.setId(counter.incrementAndGet());

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

    public HashMap<Integer, User> getUsers() {
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

    public ArrayList<User> getFriends(int id) {

        HashSet<Integer> friendsIds = userStorage.getUserById(id).getFriends();
        ArrayList<User> friends = new ArrayList<>();

        friendsIds.forEach(idFormSet -> friends.add(userStorage.getUserById(idFormSet)));

        return friends;
    }

    public ArrayList<User> getCommonFriends(int id, int otherId) {

        HashSet<Integer> myFriendsIds = userStorage.getUserById(id).getFriends();
        HashSet<Integer> otherFriendsIds = userStorage.getUserById(otherId).getFriends();
        ArrayList<User> commonFriends = new ArrayList<>();

        myFriendsIds.stream()
                .filter(otherFriendsIds::contains)
                .forEach(commonId -> commonFriends.add(userStorage.getUserById(commonId)));

        return commonFriends;
    }

    public void clear() {
        userStorage.getUsers().clear();
        counter = new AtomicInteger(0);
    }
}
