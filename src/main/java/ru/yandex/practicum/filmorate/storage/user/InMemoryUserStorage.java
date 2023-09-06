package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FriendShip;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryUserStorage implements UserStorage {

    private final AtomicInteger counter = new AtomicInteger(0);
    private final Collection<User> users = new ArrayList<>();

    @Override
    public Optional<User> postUser(User user) {
        user.setId(counter.incrementAndGet());
        users.add(user);
        return getUserById(user.getId());
    }

    @Override
    public Optional<User> putUser(User user) {
        Optional<User> userToRemove = getUserById(user.getId());
        if (userToRemove.isEmpty()) {
            return Optional.empty();
        }

        users.remove(userToRemove.get());
        users.add(user);
        return getUserById(user.getId());
    }

    @Override
    public Collection<User> getUsers() {
        return users;
    }

    @Override
    public Optional<User> getUserById(int id) {
        return users.stream().filter(u -> u.getId() == id).findFirst();
    }

    @Override
    public Optional<User> putUserFriend(int userId, int friendId) {

        User user = getUserById(friendId).get();
        user.getFriendShips().add(new FriendShip(userId, friendId));

        return getUserById(userId);

    }

    @Override
    public Optional<User> deleteUserFriend(int userId, int friendId) {

        User user = getUserById(friendId).get();
        user.getFriendShips().remove(new FriendShip(userId, friendId));

        return getUserById(userId);
    }

    @Override
    public Collection<User> getFriends(int userId) {
        Collection<User> friends = new HashSet<>();

        if (getUserById(userId).isEmpty()) {
            return friends;
        }

        User user = getUserById(userId).get();
        for (FriendShip friendship : user.getFriendShips()) {
            if (getUserById(friendship.getFriendId()).isEmpty()) {
                continue;
            }
            User friend = getUserById(friendship.getFriendId()).get();
            friends.add(friend);
        }
        return friends;
    }

    @Override
    public Collection<User> getCommonFriends(int userId, int otherId) {
        Collection<User> myFriends = new HashSet<>(getFriends(userId));
        Collection<User> otherFriends = new HashSet<>(getFriends(otherId));

        myFriends.retainAll(otherFriends);

        return myFriends;
    }

    @Override
    public void deleteUser(int userId) {
        if (!users.contains(userId)) {
            throw new NotFoundException("Такого id " + userId + " пользователя нет чтобы удалить.");
        }
        users.remove(userId);
    }

    @Override
    public HashMap<Integer, HashMap<Integer, Film>> getRecommendationFilms(int id) {
        return null;
    }

}
