package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class InMemoryUserStorage implements UserStorage {

    private AtomicInteger counter = new AtomicInteger(0);
    private final HashMap<Integer, User> users = new HashMap<>();

    @Override
    public User postUser(User user) {
        user.setId(counter.incrementAndGet());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User putUser(User user) {
        users.replace(user.getId(), user);
        return user;
    }

    @Override
    public HashMap<Integer, User> getUsers() {
        return users;
    }

    @Override
    public User getUserById(int id) {
        return users.get(id);
    }

    @Override
    public void clear() {
        users.clear();
        counter = new AtomicInteger(0);
    }
}